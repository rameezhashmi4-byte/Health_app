package com.pushprime.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

/**
 * Exercise Log Entity
 * Stores individual exercise sessions in Room database
 * Future: Can be synced 1:1 with Firebase "exercise_logs" collection
 */
@Entity(tableName = "exercise_logs")
data class ExerciseLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val exerciseName: String, // e.g., "Push-ups", "Sit-ups", "Plank"
    val repsOrDuration: Int, // Reps for count-based, seconds for time-based
    val timestamp: Long = System.currentTimeMillis(),
    val intensity: Int = 3, // 1-5 stars
    val workoutDuration: Int = 0, // Total workout time in seconds
    val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
) {
    /**
     * Check if exercise is time-based (e.g., Plank)
     */
    fun isTimeBased(): Boolean {
        return exerciseName.lowercase() in listOf("plank", "wall sit", "hold")
    }
    
    /**
     * Get formatted value (reps or time)
     */
    fun getFormattedValue(): String {
        return if (isTimeBased()) {
            formatTime(repsOrDuration)
        } else {
            "$repsOrDuration reps"
        }
    }
    
    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return if (minutes > 0) {
            "${minutes}m ${secs}s"
        } else {
            "${secs}s"
        }
    }
    
    /**
     * Get formatted date
     */
    fun getFormattedDate(): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }
}

/**
 * Supported exercises enum
 */
enum class ExerciseType(
    val displayName: String,
    val isTimeBased: Boolean = false,
    val icon: String = "💪"
) {
    PUSH_UPS("Push-ups", false, "💪"),
    SIT_UPS("Sit-ups", false, "🏋️"),
    SQUATS("Squats", false, "🦵"),
    PULL_UPS("Pull-ups", false, "🤸"),
    PLANK("Plank", true, "⏱️"),
    JUMPING_JACKS("Jumping Jacks", false, "🤾"),
    BURPEES("Burpees", false, "🔥"),
    LUNGES("Lunges", false, "🚶"),
    MOUNTAIN_CLIMBERS("Mountain Climbers", false, "🏃"),
    HIGH_KNEES("High Knees", false, "🦘");
    
    companion object {
        fun fromDisplayName(name: String): ExerciseType? {
            return values().find { it.displayName == name }
        }
    }
}
