package com.pushprime.data

import com.pushprime.model.SessionEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val sessionDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

fun todaySessionDate(): String = sessionDateFormatter.format(Date())

fun dateFromTimestamp(timestamp: Long): String = sessionDateFormatter.format(Date(timestamp))

fun totalRepsForDate(sessions: List<SessionEntity>, date: String): Int {
    return sessions.filter { it.date == date }.sumOf { it.totalReps ?: 0 }
}

fun totalDurationSecondsForDate(sessions: List<SessionEntity>, date: String): Int {
    return sessions
        .filter { it.date == date }
        .sumOf { it.totalSeconds ?: it.getDurationSeconds() }
}

fun totalDurationSeconds(sessions: List<SessionEntity>): Int {
    return sessions.sumOf { it.totalSeconds ?: it.getDurationSeconds() }
}

fun calculateStreak(sessions: List<SessionEntity>): Int {
    if (sessions.isEmpty()) return 0
    val availableDates = sessions.map { it.date }.toSet()
    return calculateStreakFromDates(availableDates)
}

fun calculateStreakFromDates(availableDates: Set<String>): Int {
    if (availableDates.isEmpty()) return 0
    val calendar = Calendar.getInstance()
    var streak = 0
    while (true) {
        val dateKey = sessionDateFormatter.format(calendar.time)
        if (availableDates.contains(dateKey)) {
            streak++
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        } else {
            break
        }
    }
    return streak
}

fun latestSession(sessions: List<SessionEntity>): SessionEntity? {
    return sessions.maxByOrNull { it.startTime }
}
