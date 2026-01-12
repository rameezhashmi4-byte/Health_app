package com.pushprime.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pushprime.data.SpotifyHelper
import com.pushprime.data.WorkoutPlaylist
import com.pushprime.data.Track
import com.pushprime.ui.theme.PushPrimeColors

/**
 * Spotify Browser Screen
 * Browse and play workout playlists
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotifyBrowserScreen(
    spotifyHelper: SpotifyHelper?,
    onNavigateBack: () -> Unit,
    onPlaylistSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnected = if (spotifyHelper != null) {
        spotifyHelper.isConnected.collectAsState(initial = false).value
    } else {
        remember { mutableStateOf(false) }.value
    }
    
    val currentTrack = if (spotifyHelper != null) {
        spotifyHelper.currentTrack.collectAsState(initial = null).value
    } else {
        remember { mutableStateOf<Track?>(null) }.value
    }
    
    val isPlaying = if (spotifyHelper != null) {
        spotifyHelper.isPlaying.collectAsState(initial = false).value
    } else {
        remember { mutableStateOf(false) }.value
    }
    
    val playlists = remember {
        spotifyHelper?.getWorkoutPlaylists() ?: emptyList()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Workout Music",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isConnected) {
                        IconButton(onClick = { spotifyHelper?.disconnect() }) {
                            Icon(Icons.Default.Logout, contentDescription = "Disconnect")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PushPrimeColors.Surface
                )
            )
        },
        bottomBar = {
            if (isConnected && currentTrack != null) {
                SpotifyNowPlayingBar(
                    track = currentTrack!!,
                    isPlaying = isPlaying,
                    onPlayPause = {
                        if (isPlaying) {
                            spotifyHelper?.pause()
                        } else {
                            spotifyHelper?.resume()
                        }
                    },
                    onNext = { spotifyHelper?.skipNext() },
                    onPrevious = { spotifyHelper?.skipPrevious() }
                )
            }
        },
        modifier = modifier
    ) { paddingValues ->
        if (!isConnected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = "Not Connected",
                        modifier = Modifier.size(64.dp),
                        tint = PushPrimeColors.OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Not Connected",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Connect to Spotify to browse playlists",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PushPrimeColors.OnSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Workout Playlists",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                items(playlists) { playlist ->
                    PlaylistCard(
                        playlist = playlist,
                        onClick = {
                            onPlaylistSelected(playlist.uri)
                            spotifyHelper?.playPlaylist(playlist.uri)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PlaylistCard(
    playlist: WorkoutPlaylist,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = PushPrimeColors.Surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1DB954).copy(alpha = 0.2f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            Icons.Default.PlaylistPlay,
                            contentDescription = null,
                            tint = Color(0xFF1DB954),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = playlist.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = playlist.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = PushPrimeColors.OnSurfaceVariant
                    )
                }
            }
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = Color(0xFF1DB954),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun SpotifyNowPlayingBar(
    track: Track,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = PushPrimeColors.Surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = "Now Playing",
                    tint = Color(0xFF1DB954),
                    modifier = Modifier.size(24.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                    Text(
                        text = track.artist.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = PushPrimeColors.OnSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevious, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = Color(0xFF1DB954),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onPlayPause, modifier = Modifier.size(48.dp)) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color(0xFF1DB954),
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = onNext, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = Color(0xFF1DB954),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
