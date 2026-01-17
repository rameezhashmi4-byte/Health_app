package com.pushprime.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pushprime.voice.CoachFrequency
import com.pushprime.voice.CoachPersonality
import com.pushprime.voice.VoiceCoachSettings
import com.pushprime.voice.VoiceProviderType
import com.pushprime.voice.VoiceType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.voiceCoachDataStore by preferencesDataStore(name = "voice_coach_settings")

class VoiceCoachSettingsRepository(
    private val context: Context
) {
    private object Keys {
        val Enabled = booleanPreferencesKey("voice_coach_enabled")
        val Personality = stringPreferencesKey("voice_coach_personality")
        val Frequency = stringPreferencesKey("voice_coach_frequency")
        val VoiceType = stringPreferencesKey("voice_coach_voice_type")
        val RestOnly = booleanPreferencesKey("voice_coach_rest_only")
        val Provider = stringPreferencesKey("voice_coach_provider")
    }

    val settings: Flow<VoiceCoachSettings> = context.voiceCoachDataStore.data.map { prefs ->
        VoiceCoachSettings(
            enabled = prefs[Keys.Enabled] ?: true,
            personality = readEnum(prefs[Keys.Personality], CoachPersonality.FRIENDLY),
            frequency = readEnum(prefs[Keys.Frequency], CoachFrequency.MEDIUM),
            voiceType = readEnum(prefs[Keys.VoiceType], VoiceType.SYSTEM_DEFAULT),
            speakDuringRestOnly = prefs[Keys.RestOnly] ?: false,
            provider = readEnum(prefs[Keys.Provider], VoiceProviderType.SYSTEM)
        )
    }

    suspend fun updateEnabled(enabled: Boolean) {
        context.voiceCoachDataStore.edit { it[Keys.Enabled] = enabled }
    }

    suspend fun updatePersonality(personality: CoachPersonality) {
        context.voiceCoachDataStore.edit { it[Keys.Personality] = personality.name }
    }

    suspend fun updateFrequency(frequency: CoachFrequency) {
        context.voiceCoachDataStore.edit { it[Keys.Frequency] = frequency.name }
    }

    suspend fun updateVoiceType(voiceType: VoiceType) {
        context.voiceCoachDataStore.edit { it[Keys.VoiceType] = voiceType.name }
    }

    suspend fun updateRestOnly(enabled: Boolean) {
        context.voiceCoachDataStore.edit { it[Keys.RestOnly] = enabled }
    }

    suspend fun updateProvider(provider: VoiceProviderType) {
        context.voiceCoachDataStore.edit { it[Keys.Provider] = provider.name }
    }

    private inline fun <reified T : Enum<T>> readEnum(
        value: String?,
        fallback: T
    ): T {
        if (value.isNullOrBlank()) return fallback
        return runCatching { enumValueOf<T>(value) }.getOrElse { fallback }
    }
}
