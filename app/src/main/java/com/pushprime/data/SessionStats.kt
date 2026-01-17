package com.pushprime.data

import com.pushprime.model.SessionEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object SessionStats {
    fun calculateStreak(sessions: List<SessionEntity>, today: Date = Date()): Int {
        if (sessions.isEmpty()) return 0
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val uniqueDates = sessions.map { it.date }.distinct().sortedDescending()
        val todayStr = formatter.format(today)
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time
        val yesterdayStr = formatter.format(yesterday)

        if (uniqueDates.first() != todayStr && uniqueDates.first() != yesterdayStr) return 0

        var streak = 0
        val cal = Calendar.getInstance()
        while (true) {
            val dateStr = formatter.format(cal.time)
            if (uniqueDates.contains(dateStr)) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }

    fun calculateWeeklySessions(sessions: List<SessionEntity>): Int {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val weekStart = formatter.format(cal.time)
        return sessions.count { it.date >= weekStart }
    }

    fun calculateWeeklyProgress(sessions: List<SessionEntity>): List<Int> {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val progress = MutableList(7) { 0 }
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -6)
        for (i in 0..6) {
            val dateStr = formatter.format(cal.time)
            progress[i] = sessions.count { it.date == dateStr }
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return progress
    }

    fun calculateTotalDaysActive(sessions: List<SessionEntity>): Int {
        return sessions.map { it.date }.distinct().size
    }
}
