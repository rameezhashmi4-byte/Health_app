package com.pushprime.model

/**
 * Session model
 * Represents a workout session
 */
data class Session(
    val id: String = "",
    val username: String = "",
    val userId: String = "",
    val pushups: Int = 0,
    val workoutTime: Int = 0, // in seconds
    val timestamp: Long = System.currentTimeMillis(),
    val country: String = "US",
    val date: String = "" // YYYY-MM-DD format
) {
    fun getFormattedTime(): String {
        val hours = workoutTime / 3600
        val minutes = (workoutTime % 3600) / 60
        val seconds = workoutTime % 60
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }
}
