package com.pushprime.data.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

object NotificationScheduler {
    const val DAILY_REMINDER_WORK_NAME = "RAMBOOST_DAILY_REMINDER"
    const val STREAK_PROTECTION_WORK_NAME = "RAMBOOST_STREAK_PROTECT"

    suspend fun resync(context: Context) {
        val prefs = NotificationPreferencesStore(context).getPreferences()
        if (!prefs.notificationsEnabled) {
            cancelDailyReminder(context)
            cancelStreakProtection(context)
            return
        }

        if (prefs.dailyReminderEnabled) {
            scheduleDailyReminder(context, prefs.dailyReminderTimeMinutes)
        } else {
            cancelDailyReminder(context)
        }

        if (prefs.streakProtectionEnabled) {
            scheduleStreakProtection(context, NotificationTimeUtils.DEFAULT_STREAK_PROTECTION_MINUTES)
        } else {
            cancelStreakProtection(context)
        }
    }

    fun scheduleDailyReminder(context: Context, timeMinutes: Int) {
        NotificationHelper(context).createChannels()
        val delay = calculateInitialDelay(timeMinutes)
        val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DAILY_REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancelDailyReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(DAILY_REMINDER_WORK_NAME)
    }

    fun scheduleStreakProtection(context: Context, timeMinutes: Int) {
        NotificationHelper(context).createChannels()
        val delay = calculateInitialDelay(timeMinutes)
        val request = PeriodicWorkRequestBuilder<StreakProtectionWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            STREAK_PROTECTION_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancelStreakProtection(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(STREAK_PROTECTION_WORK_NAME)
    }

    private fun calculateInitialDelay(targetMinutes: Int): Long {
        val targetTime = NotificationTimeUtils.minutesToLocalTime(targetMinutes)
        val now = ZonedDateTime.now()
        var scheduled = now.withHour(targetTime.hour)
            .withMinute(targetTime.minute)
            .withSecond(0)
            .withNano(0)
        if (!scheduled.isAfter(now)) {
            scheduled = scheduled.plusDays(1)
        }
        return Duration.between(now, scheduled).toMillis().coerceAtLeast(0L)
    }
}
