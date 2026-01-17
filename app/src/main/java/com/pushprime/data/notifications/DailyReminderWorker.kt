package com.pushprime.data.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pushprime.navigation.Screen

class DailyReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return try {
            val prefs = NotificationPreferencesStore(applicationContext).getPreferences()
            if (!prefs.notificationsEnabled || !prefs.dailyReminderEnabled) {
                return Result.success()
            }
            if (!NotificationPermissions.isGranted(applicationContext)) {
                return Result.success()
            }

            val helper = NotificationHelper(applicationContext)
            helper.createChannels()
            helper.showNotification(
                channelId = RamboostNotificationChannels.DAILY_REMINDER_ID,
                title = RamboostNotificationMessages.DAILY_TITLE,
                message = RamboostNotificationMessages.DAILY_MESSAGE,
                deepLinkRoute = Screen.SessionMode.route
            )
            Result.success()
        } catch (_: Exception) {
            Result.success()
        }
    }
}
