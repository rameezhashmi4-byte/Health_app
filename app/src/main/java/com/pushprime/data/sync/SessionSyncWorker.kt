package com.pushprime.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.pushprime.data.AppDatabase
import com.pushprime.data.SessionRepository
import com.pushprime.model.SessionEntity
import kotlinx.coroutines.tasks.await

class SessionSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    private val database = AppDatabase.getDatabase(appContext)
    private val repository = SessionRepository(database.sessionDao())

    override suspend fun doWork(): Result {
        val firestore = try {
            FirebaseFirestore.getInstance()
        } catch (_: Exception) {
            return Result.retry()
        }

        val unsynced = repository.getUnsyncedSessions()
        if (unsynced.isEmpty()) {
            return Result.success()
        }

        var anyFailure = false
        unsynced.forEach { session ->
            if (session.userId.isBlank() || session.userId == "anonymous") {
                repository.markSynced(session.id, System.currentTimeMillis())
                return@forEach
            }
            val success = uploadSession(firestore, session)
            if (success) {
                repository.markSynced(session.id, System.currentTimeMillis())
            } else {
                repository.incrementSyncAttempt(session.id)
                anyFailure = true
            }
        }

        return if (anyFailure) Result.retry() else Result.success()
    }

    private suspend fun uploadSession(
        firestore: FirebaseFirestore,
        session: SessionEntity
    ): Boolean {
        return try {
            val data = hashMapOf(
                "userId" to session.userId,
                "startTime" to session.startTime,
                "endTime" to session.endTime,
                "activityType" to session.activityType,
                "exerciseId" to session.exerciseId,
                "sportType" to session.sportType,
                "mode" to session.mode,
                "totalReps" to session.totalReps,
                "totalSeconds" to session.totalSeconds,
                "intensity" to session.intensity,
                "tags" to session.tags,
                "notes" to session.notes,
                "date" to session.date
            )
            firestore.collection("users")
                .document(session.userId)
                .collection("workoutSessions")
                .document(session.id.toString())
                .set(data)
                .await()
            true
        } catch (_: Exception) {
            false
        }
    }
}
