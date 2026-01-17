package com.pushprime.data.notifications

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.smartReminderDataStore by preferencesDataStore(name = "smart_reminder_prefs")

data class SmartReminderPreferences(
    val notificationsEnabled: Boolean = true,
    val smartRemindersEnabled: Boolean = true,
    val quietHoursEnabled: Boolean = true,
    val lastReminderSentAt: Long = 0L
)

class SmartReminderPreferencesStore(private val context: Context) {
    private object Keys {
        val notificationsEnabled = booleanPreferencesKey("notifications_enabled")
        val smartRemindersEnabled = booleanPreferencesKey("smart_reminders_enabled")
        val quietHoursEnabled = booleanPreferencesKey("quiet_hours_enabled")
        val lastReminderSentAt = longPreferencesKey("last_reminder_sent_at")
    }

    val preferencesFlow: Flow<SmartReminderPreferences> = context.smartReminderDataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            SmartReminderPreferences(
                notificationsEnabled = prefs[Keys.notificationsEnabled] ?: true,
                smartRemindersEnabled = prefs[Keys.smartRemindersEnabled] ?: true,
                quietHoursEnabled = prefs[Keys.quietHoursEnabled] ?: true,
                lastReminderSentAt = prefs[Keys.lastReminderSentAt] ?: 0L
            )
        }

    suspend fun getPreferences(): SmartReminderPreferences {
        return preferencesFlow.first()
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.smartReminderDataStore.edit { prefs ->
            prefs[Keys.notificationsEnabled] = enabled
        }
    }

    suspend fun setSmartRemindersEnabled(enabled: Boolean) {
        context.smartReminderDataStore.edit { prefs ->
            prefs[Keys.smartRemindersEnabled] = enabled
        }
    }

    suspend fun setQuietHoursEnabled(enabled: Boolean) {
        context.smartReminderDataStore.edit { prefs ->
            prefs[Keys.quietHoursEnabled] = enabled
        }
    }

    suspend fun setLastReminderSentAt(timestamp: Long) {
        context.smartReminderDataStore.edit { prefs ->
            prefs[Keys.lastReminderSentAt] = timestamp
        }
    }
}
