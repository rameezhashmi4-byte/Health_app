package com.pushprime.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pushprime.model.LeaderboardEntry
import com.pushprime.model.Session
import kotlinx.coroutines.tasks.await

/**
 * Firebase Helper
 * Manages Firebase Firestore operations for global leaderboard
 * Collection: "user_sessions"
 * Fields: pushups (int), workoutTime (int), timestamp (auto), username (string)
 * MVP: Safe initialization - returns empty results if Firebase not configured
 */
class FirebaseHelper {
    private val db: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (e: Exception) {
        null // Firebase not configured - work in offline mode
    }
    private val collectionName = "user_sessions"
    val isAvailable: Boolean get() = db != null
    
    /**
     * Save session to Firebase
     */
    suspend fun saveSession(session: Session): Result<Unit> {
        if (db == null) {
            return Result.failure(Exception("Firebase not configured"))
        }
        return try {
            val data = hashMapOf(
                "username" to session.username,
                "pushups" to session.pushups,
                "workoutTime" to session.workoutTime,
                "timestamp" to session.timestamp,
                "country" to session.country,
                "date" to session.date
            )
            
            db.collection(collectionName)
                .add(data)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get global leaderboard (top N entries)
     */
    suspend fun getGlobalLeaderboard(limit: Int = 100): List<LeaderboardEntry> {
        if (db == null) {
            return emptyList() // Firebase not available - return empty
        }
        return try {
            val snapshot = db.collection(collectionName)
                .orderBy("pushups", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            
            snapshot.documents.mapIndexed { index, document ->
                LeaderboardEntry(
                    rank = index + 1,
                    username = document.getString("username") ?: "",
                    pushups = document.getLong("pushups")?.toInt() ?: 0,
                    workoutTime = document.getLong("workoutTime")?.toInt() ?: 0,
                    country = document.getString("country") ?: "US",
                    date = document.getString("date") ?: "",
                    timestamp = document.getLong("timestamp") ?: 0L,
                    isLocal = false
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Get user's best session
     */
    suspend fun getUserBestSession(username: String): LeaderboardEntry? {
        if (db == null) {
            return null // Firebase not available
        }
        return try {
            val snapshot = db.collection(collectionName)
                .whereEqualTo("username", username)
                .orderBy("pushups", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
            
            if (snapshot.documents.isNotEmpty()) {
                val doc = snapshot.documents[0]
                LeaderboardEntry(
                    rank = 0,
                    username = doc.getString("username") ?: "",
                    pushups = doc.getLong("pushups")?.toInt() ?: 0,
                    workoutTime = doc.getLong("workoutTime")?.toInt() ?: 0,
                    country = doc.getString("country") ?: "US",
                    date = doc.getString("date") ?: "",
                    timestamp = doc.getLong("timestamp") ?: 0L,
                    isLocal = false
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
