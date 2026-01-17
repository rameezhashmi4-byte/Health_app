package com.pushprime.data

import android.app.Activity
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Dummy types for when Spotify SDK is not available
data class Track(
    val name: String,
    val artist: Artist,
    val uri: String
)

data class Artist(
    val name: String
)

/**
 * Spotify Helper - Placeholder implementation
 * Handles Spotify authentication and music playback
 */
class SpotifyHelper(private val context: Context) {
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    companion object {
        private const val REDIRECT_URI = "pushprime://callback"
    }
    
    fun connect(activity: Activity, onError: (Throwable) -> Unit = {}) {
        // Dummy implementation
        _isConnected.value = true
        _isPlaying.value = true
        _currentTrack.value = Track("Mock Track", Artist("Mock Artist"), "spotify:track:mock")
    }
    
    fun disconnect() {
        _isConnected.value = false
        _currentTrack.value = null
        _isPlaying.value = false
    }
    
    fun playTrack(uri: String) {
        _isPlaying.value = true
    }
    
    fun playPlaylist(uri: String) {
        playTrack(uri)
    }
    
    fun resume() {
        _isPlaying.value = true
    }
    
    fun pause() {
        _isPlaying.value = false
    }
    
    fun skipNext() {}
    
    fun skipPrevious() {}
    
    fun getWorkoutPlaylists(): List<WorkoutPlaylist> {
        return listOf(
            WorkoutPlaylist(
                name = "Workout Mix",
                uri = "spotify:playlist:37i9dQZF1DX76Wlfdnj3AP",
                description = "High-energy tracks for your workout"
            )
        )
    }
}

data class WorkoutPlaylist(
    val name: String,
    val uri: String,
    val description: String
)
