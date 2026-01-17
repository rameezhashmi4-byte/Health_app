package com.pushprime.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pushprime.coach.CoachFrequency
import com.pushprime.coach.CoachIntelligence
import com.pushprime.coach.CoachSettings
import com.pushprime.coach.CoachStyle
import com.pushprime.coach.VoiceProviderType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.coachSettingsDataStore by preferencesDataStore(name = "coach_settings")

class CoachSettingsRepository(
    private val context: Context
) {
    private object Keys {
        val HybridEnabled = booleanPreferencesKey("hybrid_enabled")
        val CoachIntelligence = stringPreferencesKey("coach_intelligence")
        val VoiceProvider = stringPreferencesKey("voice_provider")
        val CoachStyle = stringPreferencesKey("coach_style")
        val CoachFrequency = stringPreferencesKey("coach_frequency")
        val SpeakReplies = booleanPreferencesKey("speak_replies")
    }

    val settings: Flow<CoachSettings> = context.coachSettingsDataStore.data.map { prefs ->
        CoachSettings(
            hybridEnabled = prefs[Keys.HybridEnabled] ?: false,
            intelligence = enumValueOrDefault(
                prefs[Keys.CoachIntelligence],
                CoachIntelligence.BASIC
            ),
            voiceProvider = enumValueOrDefault(
                prefs[Keys.VoiceProvider],
                VoiceProviderType.SYSTEM
            ),
            style = enumValueOrDefault(prefs[Keys.CoachStyle], CoachStyle.FRIENDLY),
            frequency = enumValueOrDefault(prefs[Keys.CoachFrequency], CoachFrequency.MEDIUM),
            speakReplies = prefs[Keys.SpeakReplies] ?: false
        )
    }

    suspend fun setHybridEnabled(value: Boolean) {
        context.coachSettingsDataStore.edit { it[Keys.HybridEnabled] = value }
    }

    suspend fun setCoachIntelligence(value: CoachIntelligence) {
        context.coachSettingsDataStore.edit { it[Keys.CoachIntelligence] = value.name }
    }

    suspend fun setVoiceProvider(value: VoiceProviderType) {
        context.coachSettingsDataStore.edit { it[Keys.VoiceProvider] = value.name }
    }

    suspend fun setCoachStyle(value: CoachStyle) {
        context.coachSettingsDataStore.edit { it[Keys.CoachStyle] = value.name }
    }

    suspend fun setCoachFrequency(value: CoachFrequency) {
        context.coachSettingsDataStore.edit { it[Keys.CoachFrequency] = value.name }
    }

    suspend fun setSpeakReplies(value: Boolean) {
        context.coachSettingsDataStore.edit { it[Keys.SpeakReplies] = value }
    }

    private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String?, fallback: T): T {
        return try {
            if (value.isNullOrBlank()) fallback else enumValueOf(value)
        } catch (_: Exception) {
            fallback
        }
    }
}
