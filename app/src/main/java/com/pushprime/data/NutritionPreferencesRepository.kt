package com.pushprime.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.nutritionSettingsStore by preferencesDataStore(name = "nutrition_settings")

class NutritionPreferencesRepository(
    private val context: Context
) {
    private object Keys {
        val CalorieGoal = intPreferencesKey("calorie_goal")
        val ProteinGoal = intPreferencesKey("protein_goal")
    }

    val calorieGoal: Flow<Int> = context.nutritionSettingsStore.data.map { prefs ->
        prefs[Keys.CalorieGoal] ?: 2200
    }

    val proteinGoal: Flow<Int> = context.nutritionSettingsStore.data.map { prefs ->
        prefs[Keys.ProteinGoal] ?: 150
    }

    suspend fun updateCalorieGoal(value: Int) {
        context.nutritionSettingsStore.edit { prefs ->
            prefs[Keys.CalorieGoal] = value
        }
    }

    suspend fun updateProteinGoal(value: Int) {
        context.nutritionSettingsStore.edit { prefs ->
            prefs[Keys.ProteinGoal] = value
        }
    }
}
