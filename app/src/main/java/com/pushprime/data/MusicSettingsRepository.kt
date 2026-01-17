package com.pushprime.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pushprime.music.BpmPlaylistMapping
import com.pushprime.music.EnergyLevel
import com.pushprime.music.MusicSessionType
import com.pushprime.music.MusicSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.musicSettingsDataStore by preferencesDataStore(name = "music_settings")

class MusicSettingsRepository(
    private val context: Context
) {
    private object Keys {
        val MusicSource = stringPreferencesKey("music_source")
        val EnergyLevel = stringPreferencesKey("music_energy_level")
        val BaseBpm = intPreferencesKey("music_base_bpm")
        val AutopilotEnabled = booleanPreferencesKey("music_autopilot_enabled")
        val SessionType = stringPreferencesKey("music_session_type")
        val PlaylistMappings = stringPreferencesKey("music_playlist_mappings")
    }

    val musicSource: Flow<MusicSource> = context.musicSettingsDataStore.data.map { prefs ->
        val raw = prefs[Keys.MusicSource] ?: MusicSource.BASIC.name
        runCatching { MusicSource.valueOf(raw) }.getOrDefault(MusicSource.BASIC)
    }

    val energyLevel: Flow<EnergyLevel> = context.musicSettingsDataStore.data.map { prefs ->
        val raw = prefs[Keys.EnergyLevel] ?: EnergyLevel.FOCUS.name
        runCatching { EnergyLevel.valueOf(raw) }.getOrDefault(EnergyLevel.FOCUS)
    }

    val baseBpm: Flow<Int> = context.musicSettingsDataStore.data.map { prefs ->
        (prefs[Keys.BaseBpm] ?: 130).coerceIn(90, 180)
    }

    val autopilotEnabled: Flow<Boolean> = context.musicSettingsDataStore.data.map { prefs ->
        prefs[Keys.AutopilotEnabled] ?: true
    }

    val sessionType: Flow<MusicSessionType> = context.musicSettingsDataStore.data.map { prefs ->
        val raw = prefs[Keys.SessionType] ?: MusicSessionType.QUICK_SESSION.name
        runCatching { MusicSessionType.valueOf(raw) }.getOrDefault(MusicSessionType.QUICK_SESSION)
    }

    val bpmPlaylistMappings: Flow<List<BpmPlaylistMapping>> =
        context.musicSettingsDataStore.data.map { prefs ->
            val raw = prefs[Keys.PlaylistMappings]
            parseMappings(raw) ?: defaultMappings()
        }

    suspend fun updateMusicSource(value: MusicSource) {
        context.musicSettingsDataStore.edit { it[Keys.MusicSource] = value.name }
    }

    suspend fun updateEnergyLevel(value: EnergyLevel) {
        context.musicSettingsDataStore.edit { it[Keys.EnergyLevel] = value.name }
    }

    suspend fun updateBaseBpm(value: Int) {
        context.musicSettingsDataStore.edit { it[Keys.BaseBpm] = value.coerceIn(90, 180) }
    }

    suspend fun updateAutopilotEnabled(value: Boolean) {
        context.musicSettingsDataStore.edit { it[Keys.AutopilotEnabled] = value }
    }

    suspend fun updateSessionType(value: MusicSessionType) {
        context.musicSettingsDataStore.edit { it[Keys.SessionType] = value.name }
    }

    suspend fun updateBpmPlaylistMappings(mappings: List<BpmPlaylistMapping>) {
        context.musicSettingsDataStore.edit { it[Keys.PlaylistMappings] = serializeMappings(mappings) }
    }

    suspend fun ensureDefaultMappings() {
        val existing = context.musicSettingsDataStore.data.first()[Keys.PlaylistMappings]
        if (existing.isNullOrBlank()) {
            updateBpmPlaylistMappings(defaultMappings())
        }
    }

    private fun defaultMappings(): List<BpmPlaylistMapping> {
        return listOf(
            BpmPlaylistMapping(EnergyLevel.CHILL, 90, 120, "spotify:playlist:warmup"),
            BpmPlaylistMapping(EnergyLevel.FOCUS, 120, 150, "spotify:playlist:main"),
            BpmPlaylistMapping(EnergyLevel.BEAST_MODE, 150, 180, "spotify:playlist:finisher")
        )
    }

    private fun serializeMappings(mappings: List<BpmPlaylistMapping>): String {
        val array = JSONArray()
        mappings.forEach { mapping ->
            val obj = JSONObject().apply {
                put("energyStyle", mapping.energyStyle.name)
                put("bpmMin", mapping.bpmMin)
                put("bpmMax", mapping.bpmMax)
                put("playlistUri", mapping.playlistUri)
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun parseMappings(raw: String?): List<BpmPlaylistMapping>? {
        if (raw.isNullOrBlank()) return null
        return runCatching {
            val array = JSONArray(raw)
            val list = mutableListOf<BpmPlaylistMapping>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val energyStyle = runCatching {
                    EnergyLevel.valueOf(obj.getString("energyStyle"))
                }.getOrDefault(EnergyLevel.FOCUS)
                list.add(
                    BpmPlaylistMapping(
                        energyStyle = energyStyle,
                        bpmMin = obj.getInt("bpmMin"),
                        bpmMax = obj.getInt("bpmMax"),
                        playlistUri = obj.getString("playlistUri")
                    )
                )
            }
            list
        }.getOrNull()
    }
}
