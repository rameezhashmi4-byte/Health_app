package com.pushprime.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Workout Session model representing an active workout session
 */
@Entity(tableName = "workout_sessions")
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: String = "",
    val userId: String = "",
    val planId: Long,
    val exercisesJson: String, // JSON array of WorkoutExerciseSession
    val startedAt: Long = System.currentTimeMillis(),
    val status: SessionStatus = SessionStatus.ACTIVE,
    val currentExerciseIndex: Int = 0,
    val totalElapsedSeconds: Int = 0
) {
    fun getExercises(): List<WorkoutExerciseSession> {
        return try {
            val gson = Gson()
            val type = object : TypeToken<List<WorkoutExerciseSession>>() {}.type
            gson.fromJson(exercisesJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun withExercises(exercises: List<WorkoutExerciseSession>): WorkoutSession {
        val gson = Gson()
        return copy(exercisesJson = gson.toJson(exercises))
    }

    fun isActive(): Boolean = status == SessionStatus.ACTIVE

    companion object {
        fun create(
            userId: String,
            planId: Long,
            exercises: List<WorkoutExerciseSession>
        ): WorkoutSession {
            val gson = Gson()
            return WorkoutSession(
                sessionId = generateSessionId(),
                userId = userId,
                planId = planId,
                exercisesJson = gson.toJson(exercises),
                startedAt = System.currentTimeMillis(),
                status = SessionStatus.ACTIVE,
                currentExerciseIndex = 0
            )
        }

        private fun generateSessionId(): String {
            return "session_${System.currentTimeMillis()}_${(0..9999).random()}"
        }
    }
}

/**
 * Represents a single exercise within a workout session
 */
data class WorkoutExerciseSession(
    val name: String,
    val targetReps: Int? = null,
    val targetSeconds: Int? = null,
    val restSeconds: Int = 0,
    val cue: String = "",
    val blockType: WorkoutBlockType? = null,
    /**
     * Optional intensity tag (e.g., Light/Moderate/High/Max/Heavy) used for calorie estimation.
     */
    val intensityTag: String? = null,
    /**
     * Optional baseline calories estimate for this step, assuming a 70kg user.
     * Live estimation should refine this based on actual time spent.
     */
    val caloriesEstimate: Int? = null,
    val completedReps: Int = 0,
    val completedSeconds: Int = 0,
    val skipped: Boolean = false
) {
    fun isCompleted(): Boolean {
        return when {
            targetReps != null -> completedReps >= targetReps
            targetSeconds != null -> completedSeconds >= targetSeconds
            else -> true // Custom exercise, assume completed when manually marked
        }
    }

    fun getDisplayTarget(): String {
        return when {
            targetReps != null -> "$targetReps reps"
            targetSeconds != null -> "$targetSeconds sec"
            else -> "Custom"
        }
    }

    fun getDisplayProgress(): String {
        return when {
            targetReps != null -> "$completedReps / $targetReps reps"
            targetSeconds != null -> "$completedSeconds / $targetSeconds sec"
            else -> "In progress"
        }
    }
}

/**
 * Session status enum
 */
enum class SessionStatus(val displayName: String) {
    ACTIVE("Active"),
    PAUSED("Paused"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}