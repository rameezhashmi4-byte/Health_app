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
import kotlin.math.absoluteValue

data class ReminderNotification(
    val title: String,
    val message: String,
    val deepLinkRoute: String
)

interface NotificationProvider {
    fun send(notification: ReminderNotification)
}

object SmartReminderNotificationConstants {
    const val CHANNEL_ID = "smart_reminders"
    const val CHANNEL_NAME = "Smart Reminders"
    const val CHANNEL_DESCRIPTION = "Adaptive workout reminders"
    const val EXTRA_DEEP_LINK_ROUTE = "extra_deep_link_route"
    const val NOTIFICATION_ID_BASE = 2200
}

class LocalNotificationProvider(
    private val context: Context
) : NotificationProvider {
    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createNotificationChannel()
    }

    override fun send(notification: ReminderNotification) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(SmartReminderNotificationConstants.EXTRA_DEEP_LINK_ROUTE, notification.deepLinkRoute)
        }

        val notificationId = SmartReminderNotificationConstants.NOTIFICATION_ID_BASE +
            notification.deepLinkRoute.hashCode().absoluteValue % 100

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, SmartReminderNotificationConstants.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(notification.title)
            .setContentText(notification.message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notification.message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)

        notificationManager.notify(notificationId, builder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                SmartReminderNotificationConstants.CHANNEL_ID,
                SmartReminderNotificationConstants.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = SmartReminderNotificationConstants.CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}

class FirebaseNotificationProvider : NotificationProvider {
    override fun send(notification: ReminderNotification) {
        // Placeholder for future FCM delivery.
    }
}
