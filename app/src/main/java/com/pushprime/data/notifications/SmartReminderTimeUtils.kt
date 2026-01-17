package com.pushprime.data.notifications

import java.time.Duration
import java.time.LocalTime

object SmartReminderTimeUtils {
    val defaultReminderTime: LocalTime = LocalTime.of(19, 30)
    val defaultStreakRescueTime: LocalTime = LocalTime.of(20, 30)
    val quietHoursStart: LocalTime = LocalTime.of(22, 0)
    val quietHoursEnd: LocalTime = LocalTime.of(8, 0)
    const val habitWindowMinutes: Long = 30

    fun computeTargetTime(mostCommonWorkoutHour: Int?): LocalTime {
        return if (mostCommonWorkoutHour == null) {
            defaultReminderTime
        } else {
            LocalTime.of(mostCommonWorkoutHour.coerceIn(0, 23), 30)
        }
    }

    fun isWithinHabitWindow(now: LocalTime, targetTime: LocalTime): Boolean {
        val minutesDiff = kotlin.math.abs(Duration.between(targetTime, now).toMinutes())
        val wrapDiff = 24 * 60 - minutesDiff
        val closest = minOf(minutesDiff, wrapDiff)
        return closest <= habitWindowMinutes
    }

    fun isInQuietHours(now: LocalTime): Boolean {
        return now >= quietHoursStart || now < quietHoursEnd
    }
}
