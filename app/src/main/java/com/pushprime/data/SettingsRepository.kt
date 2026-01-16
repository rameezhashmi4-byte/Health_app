package com.pushprime.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

class SettingsRepository(
    private val context: Context
) {
    private object Keys {
        val DailyGoal = intPreferencesKey("daily_goal")
        val StreakTarget = intPreferencesKey("streak_target")
        val WorkoutReminder = booleanPreferencesKey("workout_reminder_enabled")
        val PreferredWorkout = stringPreferencesKey("preferred_workout")
    }

    val dailyGoal: Flow<Int> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.DailyGoal] ?: 100
    }

    val streakTarget: Flow<Int> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.StreakTarget] ?: 7
    }

    val workoutReminderEnabled: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.WorkoutReminder] ?: true
    }

    val preferredWorkout: Flow<String> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.PreferredWorkout] ?: "Full Body"
    }

    suspend fun updateDailyGoal(value: Int) {
        context.settingsDataStore.edit { it[Keys.DailyGoal] = value }
    }

    suspend fun updateStreakTarget(value: Int) {
        context.settingsDataStore.edit { it[Keys.StreakTarget] = value }
    }

    suspend fun updateWorkoutReminderEnabled(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.WorkoutReminder] = value }
    }

    suspend fun updatePreferredWorkout(value: String) {
        context.settingsDataStore.edit { it[Keys.PreferredWorkout] = value }
    }
}
