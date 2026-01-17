package com.pushprime.data.notifications

import java.time.LocalTime
import java.time.format.DateTimeFormatter

object NotificationTimeUtils {
    const val DEFAULT_DAILY_REMINDER_MINUTES = 19 * 60 + 30
    const val DEFAULT_STREAK_PROTECTION_MINUTES = 20 * 60 + 30

    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

    fun minutesToLocalTime(minutes: Int): LocalTime {
        val safeMinutes = minutes.coerceIn(0, 23 * 60 + 59)
        val hour = safeMinutes / 60
        val minute = safeMinutes % 60
        return LocalTime.of(hour, minute)
    }

    fun formatTime(minutes: Int): String {
        return minutesToLocalTime(minutes).format(timeFormatter)
    }
}
