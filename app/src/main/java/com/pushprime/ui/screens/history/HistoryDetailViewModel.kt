package com.pushprime.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.pushprime.data.SessionDao
import com.pushprime.model.SessionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class HistoryDetailViewModel @Inject constructor(
    private val sessionDao: SessionDao
) : ViewModel() {
    private val _session = MutableStateFlow<SessionEntity?>(null)
    val session: StateFlow<SessionEntity?> = _session.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadSession(sessionId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _session.value = sessionDao.getSessionById(sessionId)
            _isLoading.value = false
        }
    }

    fun deleteSession(session: SessionEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            sessionDao.delete(session)
            deleteRemoteSession(session)
            onComplete()
        }
    }

    fun updateSession(session: SessionEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            sessionDao.update(session)
            updateRemoteSession(session)
            _session.value = session
            onComplete()
        }
    }

    private suspend fun deleteRemoteSession(session: SessionEntity) {
        val userId = session.userId
        if (userId.isBlank() || userId == "anonymous") return
        val firestore = firestoreOrNull() ?: return
        try {
            firestore.collection("users")
                .document(userId)
                .collection("workoutSessions")
                .document(session.id.toString())
                .delete()
                .await()
        } catch (_: Exception) {
            // Remote delete is best-effort
        }
    }

    private suspend fun updateRemoteSession(session: SessionEntity) {
        val userId = session.userId
        if (userId.isBlank() || userId == "anonymous") return
        val firestore = firestoreOrNull() ?: return
        val updates = mapOf(
            "totalSeconds" to session.totalSeconds,
            "intensity" to session.intensity,
            "rating" to session.rating,
            "notes" to session.notes
        )
        try {
            firestore.collection("users")
                .document(userId)
                .collection("workoutSessions")
                .document(session.id.toString())
                .update(updates)
                .await()
        } catch (_: Exception) {
            // Remote update is best-effort
        }
    }

    private fun firestoreOrNull(): FirebaseFirestore? {
        return try {
            FirebaseFirestore.getInstance()
        } catch (_: Exception) {
            null
        }
    }
}
