package com.pushprime.data.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.pushprime.data.AppDatabase
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

object SmartReminderScheduler {
    const val WORK_NAME = "RAMBOOST_SMART_REMINDER"

    suspend fun scheduleNext(context: Context) {
        val prefs = SmartReminderPreferencesStore(context).getPreferences()
        if (!prefs.notificationsEnabled || !prefs.smartRemindersEnabled) {
            cancel(context)
            return
        }

        val sessionDao = AppDatabase.getDatabase(context).sessionDao()
        val stats = SmartReminderStatsCalculator(sessionDao).getStats()
        val targetTime = SmartReminderTimeUtils.computeTargetTime(stats.mostCommonWorkoutHour)
        scheduleAt(context, targetTime)
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    private fun scheduleAt(context: Context, targetTime: LocalTime) {
        val delayMillis = calculateInitialDelay(targetTime)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val request = OneTimeWorkRequestBuilder<SmartReminderWorker>()
            .setConstraints(constraints)
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    private fun calculateInitialDelay(targetTime: LocalTime): Long {
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
