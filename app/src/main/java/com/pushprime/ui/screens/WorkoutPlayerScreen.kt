package com.pushprime.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pushprime.data.AppDatabase
import com.pushprime.data.LocalStore
import com.pushprime.data.SessionDao
import com.pushprime.model.ActivityType
import com.pushprime.model.Intensity
import com.pushprime.model.SessionEntity
import com.pushprime.ui.theme.PushPrimeColors
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Workout Player Screen
 * REPS mode: Big counter with increment/undo
 * TIMER mode: Radial timer with pause/resume
 * Includes Spotify music player at bottom
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutPlayerScreen(
    sessionId: Long?,
    localStore: LocalStore,
    currentUserId: String?,
    spotifyHelper: com.pushprime.data.SpotifyHelper?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val database = remember { 
        try {
            AppDatabase.getDatabase(context)
        } catch (e: Exception) {
            null
        }
    }
    val sessionDao = remember(database) { database?.sessionDao() }
    val coroutineScope = rememberCoroutineScope()
    
    // Workout mode: REPS or TIMER
    var workoutMode by remember { mutableStateOf<WorkoutMode>(WorkoutMode.REPS) }
    
    // REPS mode state
    var repCount by remember { mutableStateOf(0) }
    var repHistory by remember { mutableStateOf<List<Int>>(emptyList()) }
    
    // TIMER mode state
    var timerSeconds by remember { mutableStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var timerJob by remember { mutableStateOf<Job?>(null) }
    
    // Session state
    var sessionStartTime by remember { mutableStateOf<Long?>(null) }
    var isSessionActive by remember { mutableStateOf(false) }
    
    // Spotify state - use actual SpotifyHelper if available
    val isSpotifyConnected = if (spotifyHelper != null) {
        spotifyHelper.isConnected.collectAsState(initial = false).value
    } else {
        remember { mutableStateOf(false) }.value
    }
    
    val spotifyCurrentTrack = if (spotifyHelper != null) {
        spotifyHelper.currentTrack.collectAsState(initial = null).value
    } else {
        remember { mutableStateOf<com.pushprime.data.Track?>(null) }.value
    }
    
    val isMusicPlaying = if (spotifyHelper != null) {
        spotifyHelper.isPlaying.collectAsState(initial = false).value
    } else {
        remember { mutableStateOf(false) }.value
    }
    
    val currentTrack = remember(spotifyCurrentTrack) {
        spotifyCurrentTrack?.name ?: "No track"
    }
    
    // Start session when screen loads
    LaunchedEffect(Unit) {
        sessionStartTime = System.currentTimeMillis()
        isSessionActive = true
    }
    
    // Timer logic
    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            timerJob = coroutineScope.launch {
                while (isTimerRunning) {
                    delay(1000)
                    timerSeconds++
                }
            }
        } else {
            timerJob?.cancel()
        }
    }
    
    // Save session when stopping
    fun stopSession() {
        isSessionActive = false
        timerJob?.cancel()
        isTimerRunning = false
        
        if (sessionDao != null && (repCount > 0 || timerSeconds > 0)) {
            coroutineScope.launch {
                val endTime = System.currentTimeMillis()
                val startTime = sessionStartTime ?: endTime
                val duration = ((endTime - startTime) / 1000).toInt()
                
                val session = SessionEntity(
                    userId = currentUserId,
                    startTime = startTime,
                    endTime = endTime,
                    activityType = ActivityType.GYM.name,
                    mode = workoutMode.name,
                    totalReps = if (workoutMode == WorkoutMode.REPS) repCount else null,
                    totalSeconds = if (workoutMode == WorkoutMode.TIMER) timerSeconds else duration,
                    intensity = Intensity.MEDIUM.name
                )
                sessionDao.insert(session)
            }
        }
        
        // Also save to LocalStore for backward compatibility
        if (repCount > 0) {
            val session = com.pushprime.model.Session(
                id = System.currentTimeMillis().toString(),
                username = "User",
                pushups = repCount,
                workoutTime = timerSeconds,
                timestamp = System.currentTimeMillis(),
                country = "US",
                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            )
            localStore.saveSession(session)
        }
        
        onNavigateBack()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Workout",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { stopSession() }) {
                        Icon(Icons.Default.Close, contentDescription = "End Workout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PushPrimeColors.Surface
                )
            )
        },
        bottomBar = {
            // Spotify Music Player
            SpotifyPlayerBar(
                isConnected = isSpotifyConnected,
                isPlaying = isMusicPlaying,
                currentTrack = currentTrack,
                onConnectClick = {
                    // Navigate to Spotify login - handled by parent
                },
                onPlayPauseClick = {
                    if (isMusicPlaying) {
                        spotifyHelper?.pause()
                    } else {
                        spotifyHelper?.resume()
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(PushPrimeColors.Background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Mode selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = workoutMode == WorkoutMode.REPS,
                    onClick = { workoutMode = WorkoutMode.REPS },
                    label = { Text("REPS") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = workoutMode == WorkoutMode.TIMER,
                    onClick = { workoutMode = WorkoutMode.TIMER },
                    label = { Text("TIMER") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Main workout display
            when (workoutMode) {
                WorkoutMode.REPS -> {
                    RepsCounter(
                        count = repCount,
                        onIncrement = { 
                            repCount++
                            repHistory = repHistory + repCount
                        },
                        onUndo = {
                            if (repHistory.isNotEmpty()) {
                                repHistory = repHistory.dropLast(1)
                                repCount = repHistory.lastOrNull() ?: 0
                            } else {
                                repCount = 0
                            }
                        }
                    )
                }
                WorkoutMode.TIMER -> {
                    TimerDisplay(
                        seconds = timerSeconds,
                        isRunning = isTimerRunning,
                        onPlayPause = {
                            isTimerRunning = !isTimerRunning
                        },
                        onReset = {
                            timerSeconds = 0
                            isTimerRunning = false
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Session info
            if (sessionStartTime != null) {
                val elapsed = ((System.currentTimeMillis() - sessionStartTime!!) / 1000).toInt()
                Text(
                    text = "Session: ${formatWorkoutTime(elapsed)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PushPrimeColors.OnSurfaceVariant,
                    modifier = Modifier.padding(bottom = 80.dp) // Space for music player
                )
            }
        }
    }
}

@Composable
fun RepsCounter(
    count: Int,
    onIncrement: () -> Unit,
    onUndo: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Big number display
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.displayLarge,
            fontSize = 120.sp,
            fontWeight = FontWeight.Bold,
            color = PushPrimeColors.Primary
        )
        
        // Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onIncrement,
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PushPrimeColors.Primary
                )
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Increment",
                    modifier = Modifier.size(32.dp)
                )
            }
            
            if (count > 0) {
                OutlinedButton(
                    onClick = onUndo,
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        Icons.Default.Undo,
                        contentDescription = "Undo",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TimerDisplay(
    seconds: Int,
    isRunning: Boolean,
    onPlayPause: () -> Unit,
    onReset: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Circular progress indicator
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = 1f,
                modifier = Modifier.size(200.dp),
                strokeWidth = 8.dp,
                color = PushPrimeColors.Primary.copy(alpha = 0.2f)
            )
            Text(
                text = formatWorkoutTime(seconds),
                style = MaterialTheme.typography.displayMedium,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = PushPrimeColors.Primary
            )
        }
        
        // Control buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onPlayPause,
                modifier = Modifier.size(80.dp),
                shape = CircleShape
            ) {
                Icon(
                    if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Pause" else "Play",
                    modifier = Modifier.size(32.dp)
                )
            }
            
            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.size(80.dp),
                shape = CircleShape
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Reset",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun SpotifyPlayerBar(
    isConnected: Boolean,
    isPlaying: Boolean,
    currentTrack: String?,
    onConnectClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
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
            if (!isConnected) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = "Spotify",
                        tint = Color(0xFF1DB954) // Spotify green
                    )
                    Text(
                        text = "Connect Spotify",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                TextButton(onClick = onConnectClick) {
                    Text("Connect")
                }
            } else {
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
                            text = currentTrack ?: "No track",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                        Text(
                            text = "Spotify",
                            style = MaterialTheme.typography.labelSmall,
                            color = PushPrimeColors.OnSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onPlayPauseClick) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color(0xFF1DB954)
                    )
                }
            }
        }
    }
}

fun formatWorkoutTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}

enum class WorkoutMode {
    REPS, TIMER
}
