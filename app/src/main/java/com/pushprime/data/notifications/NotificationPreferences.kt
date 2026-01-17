package com.pushprime.data.notifications

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.notificationDataStore by preferencesDataStore(name = "notification_prefs")

data class NotificationPreferences(
    val notificationsEnabled: Boolean = false,
    val dailyReminderEnabled: Boolean = true,
    val streakProtectionEnabled: Boolean = true,
    val dailyReminderTimeMinutes: Int = NotificationTimeUtils.DEFAULT_DAILY_REMINDER_MINUTES
)

class NotificationPreferencesStore(private val context: Context) {
    private object Keys {
        val notificationsEnabled = booleanPreferencesKey("notifications_enabled")
        val dailyReminderEnabled = booleanPreferencesKey("daily_reminder_enabled")
        val streakProtectionEnabled = booleanPreferencesKey("streak_protection_enabled")
        val dailyReminderTimeMinutes = intPreferencesKey("daily_reminder_time_minutes")
    }

    val preferencesFlow: Flow<NotificationPreferences> = context.notificationDataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            NotificationPreferences(
                notificationsEnabled = prefs[Keys.notificationsEnabled] ?: false,
                dailyReminderEnabled = prefs[Keys.dailyReminderEnabled] ?: true,
                streakProtectionEnabled = prefs[Keys.streakProtectionEnabled] ?: true,
                dailyReminderTimeMinutes = prefs[Keys.dailyReminderTimeMinutes]
                    ?: NotificationTimeUtils.DEFAULT_DAILY_REMINDER_MINUTES
            )
        }

    suspend fun getPreferences(): NotificationPreferences {
        return preferencesFlow.first()
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.notificationDataStore.edit { prefs ->
            prefs[Keys.notificationsEnabled] = enabled
        }
    }

    suspend fun setDailyReminderEnabled(enabled: Boolean) {
        context.notificationDataStore.edit { prefs ->
            prefs[Keys.dailyReminderEnabled] = enabled
        }
    }

    suspend fun setStreakProtectionEnabled(enabled: Boolean) {
        context.notificationDataStore.edit { prefs ->
            prefs[Keys.streakProtectionEnabled] = enabled
        }
    }

    suspend fun setDailyReminderTimeMinutes(minutes: Int) {
        context.notificationDataStore.edit { prefs ->
            prefs[Keys.dailyReminderTimeMinutes] = minutes
        }
    }
}
