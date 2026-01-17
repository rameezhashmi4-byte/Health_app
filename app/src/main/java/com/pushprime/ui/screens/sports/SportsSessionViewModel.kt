package com.pushprime.ui.screens.sports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.pushprime.data.AuthRepository
import com.pushprime.data.LocalStore
import com.pushprime.data.SessionDao
import com.pushprime.model.ActivityType
import com.pushprime.model.SessionEntity
import com.pushprime.model.SportsSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SportsSessionViewModel @Inject constructor(
    private val sessionDao: SessionDao,
    private val authRepository: AuthRepository,
    private val localStore: LocalStore
) : ViewModel() {
    private val firestore: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (_: Exception) {
        null
    }

    fun saveSession(session: SportsSession, onResult: (Result<Long>) -> Unit) {
        viewModelScope.launch {
            try {
                val totalSeconds = ((session.endTime - session.startTime) / 1000L).toInt().coerceAtLeast(0)
                val entity = SessionEntity(
                    userId = authRepository.currentUser?.uid ?: "anonymous",
                    startTime = session.startTime,
                    endTime = session.endTime,
                    activityType = ActivityType.SPORT.name,
                    sportType = session.sportType.displayName,
                    mode = "TIMER",
                    totalSeconds = totalSeconds,
                    intensity = session.effortLevel.name,
                    intervalsEnabled = session.intervalsEnabled,
                    warmupEnabled = session.warmupEnabled,
                    rating = session.rating,
                    caloriesEstimate = session.caloriesEstimate,
                    durationMinutes = session.durationMinutes,
                    notes = session.notes.ifBlank { null },
                    isSynced = false
                )
                val rowId = sessionDao.insert(entity)
                localStore.recordSessionDate(entity.date)
                onResult(Result.success(rowId))

                val currentUser = authRepository.currentUser
                if (currentUser != null && firestore != null) {
                    val data = hashMapOf(
                        "id" to session.id,
                        "sportType" to session.sportType.displayName,
                        "startTime" to session.startTime,
                        "endTime" to session.endTime,
                        "durationMinutes" to session.durationMinutes,
                        "effortLevel" to session.effortLevel.name,
                        "intervalsEnabled" to session.intervalsEnabled,
                        "warmupEnabled" to session.warmupEnabled,
                        "notes" to session.notes,
                        "rating" to session.rating,
                        "caloriesEstimate" to session.caloriesEstimate
                    )
                    firestore.collection("users")
                        .document(currentUser.uid)
                        .collection("sportsSessions")
                        .document(session.id)
                        .set(data)
                        .await()
                }
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }
}
