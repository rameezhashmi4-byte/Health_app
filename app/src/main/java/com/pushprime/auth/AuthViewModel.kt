package com.pushprime.auth

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.pushprime.data.LocalStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AuthViewModel(
    private val appContext: Context,
    private val localStore: LocalStore
) : ViewModel() {
    private val auth: FirebaseAuth? = try {
        FirebaseAuth.getInstance()
    } catch (e: Exception) {
        null
    }

    private val firestore: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (e: Exception) {
        null
    }

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isAuthReady = MutableStateFlow(false)
    val isAuthReady: StateFlow<Boolean> = _isAuthReady.asStateFlow()

    val currentUser: FirebaseUser?
        get() = auth?.currentUser

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        _isLoggedIn.value = user != null
        _isAuthReady.value = true
        if (user != null) {
            handleUserSignedIn(user)
        }
    }

    init {
        if (auth != null) {
            auth.addAuthStateListener(authStateListener)
            _isLoggedIn.value = auth.currentUser != null
            _isAuthReady.value = true
        } else {
            _isLoggedIn.value = false
            _isAuthReady.value = true
        }
    }

    override fun onCleared() {
        super.onCleared()
        auth?.removeAuthStateListener(authStateListener)
    }

    fun signInWithEmail(email: String, password: String, onResult: (Result<Unit>) -> Unit) {
        val firebaseAuth = auth ?: run {
            onResult(Result.failure(IllegalStateException("Firebase unavailable")))
            return
        }
        viewModelScope.launch {
            try {
                firebaseAuth.signInWithEmailAndPassword(email, password).await()
                onResult(Result.success(Unit))
            } catch (signInError: Exception) {
                try {
                    firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                    onResult(Result.success(Unit))
                } catch (signUpError: Exception) {
                    onResult(Result.failure(signUpError))
                }
            }
        }
    }

    fun signInWithGoogle(idToken: String, onResult: (Result<Unit>) -> Unit) {
        val firebaseAuth = auth ?: run {
            onResult(Result.failure(IllegalStateException("Firebase unavailable")))
            return
        }
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                firebaseAuth.signInWithCredential(credential).await()
                onResult(Result.success(Unit))
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }

    fun logout() {
        val userId = localStore.getCurrentSessionUserId() ?: auth?.currentUser?.uid
        val sessionId = localStore.getCurrentSessionId()
        viewModelScope.launch {
            if (userId != null && sessionId != null) {
                endSession(userId, sessionId)
            }
            localStore.clearAuthSensitiveData()
            auth?.signOut()
            localStore.clearCurrentSession()
        }
    }

    private fun handleUserSignedIn(user: FirebaseUser) {
        viewModelScope.launch {
            val currentSessionUserId = localStore.getCurrentSessionUserId()
            val currentSessionId = localStore.getCurrentSessionId()
            if (currentSessionUserId != null && currentSessionId != null && currentSessionUserId != user.uid) {
                endSession(currentSessionUserId, currentSessionId)
                localStore.clearCurrentSession()
            }
            if (localStore.getCurrentSessionId() == null) {
                startSession(user)
            }
        }
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

        val db = firestore ?: return
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
            // Ignore failures to avoid blocking the app
        }
    }

    private suspend fun endSession(userId: String, sessionId: String) {
        val db = firestore ?: return
        try {
            val updates = mapOf(
                "logoutTimestamp" to System.currentTimeMillis()
            )
            db.collection("users")
                .document(userId)
                .collection("loginSessions")
                .document(sessionId)
                .update(updates)
                .await()
        } catch (_: Exception) {
            // Ignore failures to avoid blocking the app
        }
    }

    private fun getAppVersion(): String {
        return try {
            val packageInfo = appContext.packageManager.getPackageInfo(appContext.packageName, 0)
            packageInfo.versionName ?: "unknown"
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
