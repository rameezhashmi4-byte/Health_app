package com.pushprime.auth

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.pushprime.data.AuthRepository
import com.pushprime.data.LocalStore
import com.pushprime.data.PendingSessionWrite
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AuthViewModel(
    private val appContext: Context,
    private val localStore: LocalStore,
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {
    private val firestore: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (_: Exception) {
        null
    }

    private val _isLoggedIn = MutableStateFlow(localStore.lastKnownLoggedIn.value)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    val currentUser: FirebaseUser?
        get() = authRepository.currentUser

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        _isLoggedIn.value = user != null
        localStore.setLastKnownLoggedIn(user != null)
        if (user != null) {
            viewModelScope.launch { handleUserSignedIn(user) }
        }
    }

    init {
        authRepository.addAuthStateListener(authStateListener)
        _isLoggedIn.value = authRepository.currentUser != null
        localStore.setLastKnownLoggedIn(_isLoggedIn.value)
        viewModelScope.launch { flushPendingSessionWrites() }
        authRepository.currentUser?.let { user ->
            viewModelScope.launch { handleUserSignedIn(user) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        authRepository.removeAuthStateListener(authStateListener)
    }

    fun signInWithEmail(
        email: String,
        password: String,
        isCreateAccount: Boolean,
        onResult: (Result<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (isCreateAccount) {
                    authRepository.createAccount(email, password)
                } else {
                    authRepository.signInWithEmail(email, password)
                }
                onResult(Result.success(Unit))
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }

    fun signInWithGoogle(idToken: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            try {
                authRepository.signInWithGoogle(idToken)
                onResult(Result.success(Unit))
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }

    fun logout() {
        val userId = localStore.getCurrentSessionUserId() ?: authRepository.currentUser?.uid
        val sessionId = localStore.getCurrentSessionId()
        val endedAt = System.currentTimeMillis()
        viewModelScope.launch {
            if (userId != null && sessionId != null) {
                endSession(userId, sessionId, endedAt)
            }
            localStore.clearAuthSensitiveData()
            authRepository.signOut()
            localStore.clearCurrentSession()
            localStore.setLastKnownLoggedIn(false)
        }
    }

    private suspend fun handleUserSignedIn(user: FirebaseUser) {
        val currentSessionUserId = localStore.getCurrentSessionUserId()
        val currentSessionId = localStore.getCurrentSessionId()
        if (currentSessionUserId != null && currentSessionId != null && currentSessionUserId != user.uid) {
            endSession(currentSessionUserId, currentSessionId, System.currentTimeMillis())
            localStore.clearCurrentSession()
        }
        if (localStore.getCurrentSessionId() == null) {
            startSession(user)
        }
        flushPendingSessionWrites()
    }

    private suspend fun startSession(user: FirebaseUser) {
        val loginTimestamp = System.currentTimeMillis()
        val deviceModel = listOf(Build.MANUFACTURER, Build.MODEL)
            .filter { it.isNotBlank() }
            .joinToString(" ")
        val appVersion = getAppVersion()
        val sessionId = UUID.randomUUID().toString()

        localStore.saveLoginSessionInfo(
            userId = user.uid,
            loginTimestamp = loginTimestamp,
            deviceModel = deviceModel,
            appVersion = appVersion,
            sessionId = sessionId
        )

        val db = firestore ?: run {
            localStore.addPendingSessionWrite(
                PendingSessionWrite(
                    userId = user.uid,
                    sessionId = sessionId,
                    action = "start",
                    loginTimestamp = loginTimestamp,
                    deviceModel = deviceModel,
                    appVersion = appVersion,
                    endedAt = 0L
                )
            )
            return
        }

        try {
            val data = hashMapOf(
                "userId" to user.uid,
                "loginTimestamp" to loginTimestamp,
                "deviceModel" to deviceModel,
                "appVersion" to appVersion
            )
            db.collection("users")
                .document(user.uid)
                .collection("loginSessions")
                .document(sessionId)
                .set(data)
                .await()
        } catch (_: Exception) {
            localStore.addPendingSessionWrite(
                PendingSessionWrite(
                    userId = user.uid,
                    sessionId = sessionId,
                    action = "start",
                    loginTimestamp = loginTimestamp,
                    deviceModel = deviceModel,
                    appVersion = appVersion,
                    endedAt = 0L
                )
            )
        }
    }

    private suspend fun endSession(userId: String, sessionId: String, endedAt: Long) {
        val db = firestore ?: run {
            localStore.addPendingSessionWrite(
                PendingSessionWrite(
                    userId = userId,
                    sessionId = sessionId,
                    action = "end",
                    loginTimestamp = localStore.getCurrentSessionStartedAt(),
                    deviceModel = "",
                    appVersion = "",
                    endedAt = endedAt
                )
            )
            return
        }
        try {
            val updates = mapOf(
                "endedAt" to endedAt
            )
            db.collection("users")
                .document(userId)
                .collection("loginSessions")
                .document(sessionId)
                .update(updates)
                .await()
        } catch (_: Exception) {
            localStore.addPendingSessionWrite(
                PendingSessionWrite(
                    userId = userId,
                    sessionId = sessionId,
                    action = "end",
                    loginTimestamp = localStore.getCurrentSessionStartedAt(),
                    deviceModel = "",
                    appVersion = "",
                    endedAt = endedAt
                )
            )
        }
    }

    private suspend fun flushPendingSessionWrites() {
        val db = firestore ?: return
        val pending = localStore.getPendingSessionWrites()
        pending.forEach { write ->
            try {
                if (write.action == "start") {
                    val data = hashMapOf(
                        "userId" to write.userId,
                        "loginTimestamp" to write.loginTimestamp,
                        "deviceModel" to write.deviceModel,
                        "appVersion" to write.appVersion
                    )
                    db.collection("users")
                        .document(write.userId)
                        .collection("loginSessions")
                        .document(write.sessionId)
                        .set(data)
                        .await()
                } else {
                    val updates = mapOf("endedAt" to write.endedAt)
                    db.collection("users")
                        .document(write.userId)
                        .collection("loginSessions")
                        .document(write.sessionId)
                        .update(updates)
                        .await()
                }
                localStore.removePendingSessionWrite(write.sessionId, write.action)
            } catch (_: Exception) {
                // Keep pending
            }
        }
    }

    private fun getAppVersion(): String {
        return try {
            val packageInfo = appContext.packageManager.getPackageInfo(appContext.packageName, 0)
            val versionName = packageInfo.versionName ?: "unknown"
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
            "$versionName ($versionCode)"
        } catch (_: PackageManager.NameNotFoundException) {
            "unknown"
        }
    }
}

class AuthViewModelFactory(
    private val appContext: Context,
    private val localStore: LocalStore
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(appContext, localStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
