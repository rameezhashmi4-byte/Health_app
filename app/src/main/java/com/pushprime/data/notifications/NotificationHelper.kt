package com.pushprime.data.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.pushprime.MainActivity
import com.pushprime.R

object RamboostNotificationChannels {
    const val DAILY_REMINDER_ID = "ramboost_daily_reminder"
    const val STREAK_PROTECTION_ID = "ramboost_streak_protection"

    const val DAILY_REMINDER_NAME = "Daily Reminder"
    const val STREAK_PROTECTION_NAME = "Streak Protection"
}

object RamboostNotificationMessages {
    const val DAILY_TITLE = "RAMBOOST"
    const val DAILY_MESSAGE = "Time to train \uD83D\uDCAA Start a quick session now."

    const val STREAK_TITLE = "RAMBOOST"
    const val STREAK_MESSAGE = "Protect your streak \uD83D\uDD25 Do a 10-min session to keep it alive."
}

class NotificationHelper(private val context: Context) {
    fun createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val dailyChannel = NotificationChannel(
            RamboostNotificationChannels.DAILY_REMINDER_ID,
            RamboostNotificationChannels.DAILY_REMINDER_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Daily workout reminders"
            enableVibration(true)
            enableLights(true)
        }

        val streakChannel = NotificationChannel(
            RamboostNotificationChannels.STREAK_PROTECTION_ID,
            RamboostNotificationChannels.STREAK_PROTECTION_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Streak protection reminders"
            enableVibration(true)
            enableLights(true)
        }

        manager.createNotificationChannel(dailyChannel)
        manager.createNotificationChannel(streakChannel)
    }

    fun showNotification(
        channelId: String,
        title: String,
        message: String,
        deepLinkRoute: String
    ) {
        if (!NotificationPermissions.isGranted(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(SmartReminderNotificationConstants.EXTRA_DEEP_LINK_ROUTE, deepLinkRoute)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            channelId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(channelId.hashCode(), builder.build())
    }
}
