package com.pushprime.music

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface MusicProvider {
    val isConnected: StateFlow<Boolean>
    val currentTrack: StateFlow<TrackInfo?>

    fun connect()
    fun disconnect()
    fun play()
    fun pause()
    fun skipNext()
    fun setEnergyLevel(energy: EnergyLevel)
    fun setTargetBpm(bpmRange: IntRange)
}

class BasicMusicProvider : MusicProvider {
    private val _isConnected = MutableStateFlow(true)
    private val _currentTrack = MutableStateFlow<TrackInfo?>(null)
    private val energyLevel = MutableStateFlow(EnergyLevel.FOCUS)
    private val targetRange = MutableStateFlow(120..140)

    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    override val currentTrack: StateFlow<TrackInfo?> = _currentTrack.asStateFlow()

    override fun connect() {
        _isConnected.value = true
    }

    override fun disconnect() {
        _isConnected.value = false
    }

    override fun play() {
        // No-op for basic provider
    }

    override fun pause() {
        // No-op for basic provider
    }

    override fun skipNext() {
        // No-op for basic provider
    }

    override fun setEnergyLevel(energy: EnergyLevel) {
        energyLevel.value = energy
        updateTrackInfo()
    }

    override fun setTargetBpm(bpmRange: IntRange) {
        targetRange.value = bpmRange
        updateTrackInfo()
    }

    private fun updateTrackInfo() {
        _currentTrack.value = TrackInfo(
            title = "${energyLevel.value.label} mix",
            artist = "Suggested",
            bpm = targetRange.value.last
        )
    }
}

class SpotifyMusicProvider : MusicProvider {
    private val _isConnected = MutableStateFlow(false)
    private val _currentTrack = MutableStateFlow<TrackInfo?>(null)
    private val energyLevel = MutableStateFlow(EnergyLevel.FOCUS)
    private val targetRange = MutableStateFlow(120..140)

    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    override val currentTrack: StateFlow<TrackInfo?> = _currentTrack.asStateFlow()

    override fun connect() {
        _isConnected.value = true
        _currentTrack.value = TrackInfo(
            title = "Spotify ready",
            artist = "Connect SDK",
            bpm = targetRange.value.last
        )
    }

    override fun disconnect() {
        _isConnected.value = false
        _currentTrack.value = null
    }

    override fun play() {
        // TODO: Integrate Spotify SDK playback
    }

    override fun pause() {
        // TODO: Integrate Spotify SDK playback
    }

    override fun skipNext() {
        // TODO: Integrate Spotify SDK playback
    }

    override fun setEnergyLevel(energy: EnergyLevel) {
        energyLevel.value = energy
        // TODO: Pass energy into Spotify playlist selection
    }

    override fun setTargetBpm(bpmRange: IntRange) {
        targetRange.value = bpmRange
        // TODO: Select playlist based on bpmRange and energyLevel
    }
}

object MusicProviderManager {
    private val basicProvider = BasicMusicProvider()
    private val spotifyProvider = SpotifyMusicProvider()
    private val _currentProvider = MutableStateFlow<MusicProvider>(basicProvider)

    val currentProvider: StateFlow<MusicProvider> = _currentProvider.asStateFlow()

    fun setSource(source: MusicSource) {
        _currentProvider.value = if (source == MusicSource.SPOTIFY) spotifyProvider else basicProvider
    }
}
