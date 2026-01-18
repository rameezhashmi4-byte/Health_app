package com.pushprime.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.aiCoachSettingsStore by preferencesDataStore(name = "ai_coach_settings")

enum class AiCoachMode {
    BASIC,
    OPENAI
}

data class AiCoachSettings(
    val mode: AiCoachMode = AiCoachMode.BASIC,
    val modelName: String = "gpt-4o-mini",
    val baseUrl: String = "https://api.openai.com"
)

class AiCoachSettingsRepository(
    private val context: Context
) {
    private object Keys {
        val Mode = stringPreferencesKey("ai_coach_mode")
        val ModelName = stringPreferencesKey("ai_coach_model_name")
        val BaseUrl = stringPreferencesKey("ai_coach_base_url")
    }

    val settings: Flow<AiCoachSettings> = context.aiCoachSettingsStore.data.map { prefs ->
        AiCoachSettings(
            mode = prefs[Keys.Mode]?.let { runCatching { AiCoachMode.valueOf(it) }.getOrNull() }
                ?: AiCoachMode.BASIC,
            modelName = prefs[Keys.ModelName] ?: "gpt-4o-mini",
            baseUrl = prefs[Keys.BaseUrl] ?: "https://api.openai.com"
        )
    }

    suspend fun updateMode(mode: AiCoachMode) {
        context.aiCoachSettingsStore.edit { prefs ->
            prefs[Keys.Mode] = mode.name
        }
    }

    suspend fun updateModelName(modelName: String) {
        context.aiCoachSettingsStore.edit { prefs ->
            prefs[Keys.ModelName] = modelName
        }
    }

    suspend fun updateBaseUrl(baseUrl: String) {
        context.aiCoachSettingsStore.edit { prefs ->
            prefs[Keys.BaseUrl] = baseUrl
        }
    }
}
