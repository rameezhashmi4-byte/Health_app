package com.pushprime.data.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pushprime.data.AppDatabase
import com.pushprime.data.SessionDao
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class SessionStats(
    val lastSessionAt: Long?,
    val sessionCountLast7Days: Int,
    val streakDays: Int,
    val workedOutToday: Boolean,
    val mostCommonWorkoutHour: Int?
)

class SmartReminderStatsCalculator(
    private val sessionDao: SessionDao
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    suspend fun getStats(now: ZonedDateTime = ZonedDateTime.now()): SessionStats {
        val today = now.toLocalDate()
        val todayString = today.format(dateFormatter)
        val start7Days = today.minusDays(6).format(dateFormatter)
        val startStreakWindow = today.minusDays(30).format(dateFormatter)
        val zone = now.zone

        val workedOutToday = runCatching {
            sessionDao.getSessionCountForDate(todayString)
        }.getOrDefault(0) > 0

        val sessionCountLast7Days = runCatching {
            sessionDao.getSessionCountForRange(start7Days, todayString)
        }.getOrDefault(0)

        val lastSessionAt = runCatching {
            sessionDao.getLastSessionStartTime()
        }.getOrNull()?.takeIf { it > 0L }

        val recentSessions = runCatching {
            sessionDao.getRecentSessions(14)
        }.getOrDefault(emptyList())

        val mostCommonWorkoutHour = recentSessions
            .map { Instant.ofEpochMilli(it.startTime).atZone(zone).hour }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key

        val streakSessions = runCatching {
            sessionDao.getSessionsByDateRangeOnce(startStreakWindow, todayString)
        }.getOrDefault(emptyList())

        val sessionDates = streakSessions.mapNotNull { session ->
            runCatching { LocalDate.parse(session.date, dateFormatter) }.getOrNull()
        }.toSet()

        var streakDays = 0
        var cursor = today
        while (sessionDates.contains(cursor)) {
            streakDays += 1
            cursor = cursor.minusDays(1)
        }
        if (streakDays == 0) {
            cursor = today.minusDays(1)
            while (sessionDates.contains(cursor)) {
                streakDays += 1
                cursor = cursor.minusDays(1)
            }
        }

        return SessionStats(
            lastSessionAt = lastSessionAt,
            sessionCountLast7Days = sessionCountLast7Days,
            streakDays = streakDays,
            workedOutToday = workedOutToday,
            mostCommonWorkoutHour = mostCommonWorkoutHour
        )
    }
}

enum class SmartReminderType(
    val messages: List<String>,
    val deepLinkRoute: String
) {
    STREAK_RESCUE(
        messages = listOf(
            "Protect your streak 🔥 10 mins is enough — start now.",
            "Keep the streak alive 💥 A quick session does it.",
            "Don't drop the streak ⚡ Jump in for 10 mins."
        ),
        deepLinkRoute = SmartReminderRoutes.QUICK_SESSION
    ),
    INACTIVE(
        messages = listOf(
            "RAMBOOST check-in 💪 You’ve been quiet — quick session?",
            "No training in 2 days… let’s fix that 🔥",
            "Small session today = big progress tomorrow 🚀"
        ),
        deepLinkRoute = SmartReminderRoutes.HOME
    ),
    CONSISTENCY_BOOST(
        messages = listOf(
            "Let’s build momentum 🚀 1 session today makes a difference.",
            "Momentum matters 💪 One session today keeps you moving.",
            "Consistency grows fast 🌱 A short workout today helps."
        ),
        deepLinkRoute = SmartReminderRoutes.SESSION_MODE
    )
}

object SmartReminderRoutes {
    const val HOME = "home"
    const val SESSION_MODE = "session_mode"
    const val QUICK_SESSION = "quick_session"
}

object NotificationProviderFactory {
    fun getProvider(context: Context): NotificationProvider {
        return LocalNotificationProvider(context)
    }
}

class SmartReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return try {
            val preferencesStore = SmartReminderPreferencesStore(applicationContext)
            val preferences = preferencesStore.getPreferences()

            if (!preferences.notificationsEnabled || !preferences.smartRemindersEnabled) {
                SmartReminderScheduler.cancel(applicationContext)
                return Result.success()
            }

            val now = ZonedDateTime.now()
            val nowTime = now.toLocalTime()
            val zone = now.zone

            if (preferences.quietHoursEnabled && SmartReminderTimeUtils.isInQuietHours(nowTime)) {
                SmartReminderScheduler.scheduleNext(applicationContext)
                return Result.success()
            }

            val sessionDao = AppDatabase.getDatabase(applicationContext).sessionDao()
            val stats = SmartReminderStatsCalculator(sessionDao).getStats(now)

            if (stats.workedOutToday) {
                SmartReminderScheduler.scheduleNext(applicationContext)
                return Result.success()
            }

            if (!isEligibleForSend(preferences, now, zone)) {
                SmartReminderScheduler.scheduleNext(applicationContext)
                return Result.success()
            }

            val reminderType = selectReminderType(stats, now)
            if (reminderType == null) {
                SmartReminderScheduler.scheduleNext(applicationContext)
                return Result.success()
            }

            val message = reminderType.messages.random()
            val notification = ReminderNotification(
                title = "RAMBOOST",
                message = message,
                deepLinkRoute = reminderType.deepLinkRoute
            )

            NotificationProviderFactory.getProvider(applicationContext).send(notification)
            preferencesStore.setLastReminderSentAt(now.toInstant().toEpochMilli())
            SmartReminderScheduler.scheduleNext(applicationContext)
            Result.success()
        } catch (_: Exception) {
            SmartReminderScheduler.scheduleNext(applicationContext)
            Result.success()
        }
    }

    private fun isEligibleForSend(
        preferences: SmartReminderPreferences,
        now: ZonedDateTime,
        zone: java.time.ZoneId
    ): Boolean {
        val lastSent = preferences.lastReminderSentAt
        if (lastSent <= 0L) return true

        val lastSentInstant = Instant.ofEpochMilli(lastSent)
        val lastSentDate = lastSentInstant.atZone(zone).toLocalDate()
        val today = now.toLocalDate()
        if (lastSentDate == today) return false

        val hoursSince = Duration.between(lastSentInstant, now).toHours()
        return hoursSince >= 12
    }

    private fun selectReminderType(
        stats: SessionStats,
        now: ZonedDateTime
    ): SmartReminderType? {
        val nowTime = now.toLocalTime()
        val targetTime = SmartReminderTimeUtils.computeTargetTime(stats.mostCommonWorkoutHour)

        val isLateEnoughForStreak = !nowTime.isBefore(SmartReminderTimeUtils.defaultStreakRescueTime)
        val isInactive = stats.lastSessionAt?.let { last ->
            Duration.between(Instant.ofEpochMilli(last), now.toInstant()).toHours() >= 48
        } ?: false

        return when {
            stats.streakDays > 0 && isLateEnoughForStreak -> SmartReminderType.STREAK_RESCUE
            isInactive -> SmartReminderType.INACTIVE
            stats.sessionCountLast7Days < 2 &&
                SmartReminderTimeUtils.isWithinHabitWindow(nowTime, targetTime) -> {
                SmartReminderType.CONSISTENCY_BOOST
            }
            else -> null
        }
    }
}
