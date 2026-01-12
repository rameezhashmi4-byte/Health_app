package com.pushprime.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

/**
 * Session Entity
 * Unified session tracking for both Gym and Sports activities
 */
@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val activityType: String, // "GYM" or "SPORT"
    val exerciseId: String? = null, // For gym: exercise name or ID
    val sportType: String? = null, // For sport: "Football", "Cricket", etc.
    val mode: String = "REPS", // "REPS", "TIMER", "HYBRID"
    val totalReps: Int? = null,
    val totalSeconds: Int? = null,
    val intensity: String = "MEDIUM", // "LOW", "MEDIUM", "HIGH"
    val tags: String? = null, // JSON array or comma-separated
    val notes: String? = null,
    val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
) {
    /**
     * Get duration in seconds
     */
    fun getDurationSeconds(): Int {
        return if (endTime != null) {
            ((endTime - startTime) / 1000).toInt()
        } else {
            totalSeconds ?: 0
        }
    }
    
    /**
     * Get formatted duration
     */
    fun getFormattedDuration(): String {
        val seconds = getDurationSeconds()
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format("%d:%02d", minutes, secs)
        }
    }
    
    /**
     * Check if session is active (no endTime)
     */
    fun isActive(): Boolean = endTime == null
}

/**
 * Activity Type enum
 */
enum class ActivityType(val displayName: String) {
    GYM("Gym"),
    SPORT("Sport")
}

/**
 * Sport Type enum
 */
enum class SportType(val displayName: String, val icon: String = "⚽") {
    FOOTBALL("Football", "⚽"),
    CRICKET("Cricket", "🏏"),
    RUGBY("Rugby", "🏉"),
    BASKETBALL("Basketball", "🏀"),
    TENNIS("Tennis", "🎾"),
    RUNNING("Running", "🏃"),
    CYCLING("Cycling", "🚴"),
    SWIMMING("Swimming", "🏊"),
    BOXING("Boxing", "🥊"),
    OTHER("Other", "🏅")
}

/**
 * Intensity enum
 */
enum class Intensity(val displayName: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High")
}
