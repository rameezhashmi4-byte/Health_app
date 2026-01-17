package com.pushprime.data.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pushprime.data.AppDatabase
import com.pushprime.data.LocalStore
import com.pushprime.data.NutritionRepository
import com.pushprime.data.StreakRepository
import com.pushprime.navigation.Screen
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class StreakProtectionWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    override suspend fun doWork(): Result {
        return try {
            val prefs = NotificationPreferencesStore(applicationContext).getPreferences()
            if (!prefs.notificationsEnabled || !prefs.streakProtectionEnabled) {
                return Result.success()
            }
            if (!NotificationPermissions.isGranted(applicationContext)) {
                return Result.success()
            }

            val now = ZonedDateTime.now()
            val lateEnoughTime = NotificationTimeUtils.minutesToLocalTime(
                NotificationTimeUtils.DEFAULT_STREAK_PROTECTION_MINUTES
            )
            if (now.toLocalTime().isBefore(lateEnoughTime)) {
                return Result.success()
            }

            val today = LocalDate.now()
            val todayStr = today.format(dateFormatter)
            val yesterdayStr = today.minusDays(1).format(dateFormatter)

            val database = runCatching { AppDatabase.getDatabase(applicationContext) }.getOrNull()
            val sessionDao = database?.sessionDao()
            val workedOutToday = runCatching {
                (sessionDao?.getSessionCountForDate(todayStr) ?: 0) > 0
            }.getOrDefault(false)

            val streakDays = runCatching {
                if (database == null) return@runCatching null
                val streakRepository = StreakRepository(
                    context = applicationContext,
                    sessionDao = database.sessionDao(),
                    dailyStatusDao = database.dailyStatusDao(),
                    nutritionRepository = NutritionRepository(applicationContext),
                    pullupSessionDao = database.pullupSessionDao(),
                    pullupMaxTestDao = database.pullupMaxTestDao()
                )
                streakRepository.evaluateStreak(today).currentStreakDays
            }.getOrNull()

            val fallbackStore = LocalStore(applicationContext)
            val lastSessionDate = fallbackStore.getLastSessionDate().orEmpty()
            val fallbackStreak = when {
                lastSessionDate == todayStr -> 1
                lastSessionDate == yesterdayStr -> 1
                else -> 0
            }

            val resolvedStreak = streakDays ?: fallbackStreak
            val resolvedWorkoutToday = workedOutToday || lastSessionDate == todayStr

            if (resolvedStreak > 0 && !resolvedWorkoutToday) {
                val helper = NotificationHelper(applicationContext)
                helper.createChannels()
                helper.showNotification(
                    channelId = RamboostNotificationChannels.STREAK_PROTECTION_ID,
                    title = RamboostNotificationMessages.STREAK_TITLE,
                    message = RamboostNotificationMessages.STREAK_MESSAGE,
                    deepLinkRoute = Screen.SessionMode.route
                )
            }

            Result.success()
        } catch (_: Exception) {
            Result.success()
        }
    }
}
