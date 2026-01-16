package com.pushprime.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pushprime.data.LocalStore
import com.pushprime.data.SessionDao
import com.pushprime.data.SpotifyHelper
import com.pushprime.data.calculateStreak
import com.pushprime.data.latestSession
import com.pushprime.data.todaySessionDate
import com.pushprime.data.totalRepsForDate
import com.pushprime.model.SessionEntity
import com.pushprime.ui.components.MiniMusicBar
import com.pushprime.ui.components.ProgressRing
import com.pushprime.ui.theme.PushPrimeColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Today Screen
 * Lean daily summary with core actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    localStore: LocalStore,
    sessionDao: SessionDao?,
    spotifyHelper: SpotifyHelper?,
    onNavigateToWorkout: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToNutrition: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (sessionDao == null) {
        ErrorScreen(message = "Database not available")
        return
    }

    val user by localStore.user.collectAsState(initial = null)
    val sessions by sessionDao.getAllSessions().collectAsState(initial = emptyList())

    val dailyGoal = user?.dailyGoal ?: 100
    val todayTotal = totalRepsForDate(sessions, todaySessionDate())
    val streak = calculateStreak(sessions)
    val lastSession = latestSession(sessions)

    val recommendation = remember(todayTotal, dailyGoal, streak) {
        when {
            todayTotal == 0 -> Recommendation(
                title = "Starter Set",
                subtitle = "5 min • 3 rounds of 10 reps"
            )
            todayTotal < dailyGoal / 2 -> Recommendation(
                title = "Momentum Builder",
                subtitle = "8 min • Add 20 reps"
            )
            streak >= 3 -> Recommendation(
                title = "Streak Strength",
                subtitle = "10 min • Push pace +5 reps"
            )
            else -> Recommendation(
                title = "Finish Strong",
                subtitle = "6 min • Close the gap"
            )
        }
    }

    val isSpotifyConnected = spotifyHelper?.isConnected?.collectAsState(initial = false)?.value ?: false
    val currentTrack = spotifyHelper?.currentTrack?.collectAsState(initial = null)?.value
    val isPlaying = spotifyHelper?.isPlaying?.collectAsState(initial = false)?.value ?: false

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Today",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PushPrimeColors.Surface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    onClick = onNavigateToProgress,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PushPrimeColors.Surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Progress",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "$todayTotal of $dailyGoal reps",
                                style = MaterialTheme.typography.bodyMedium,
                                color = PushPrimeColors.OnSurfaceVariant
                            )
                        }
                        ProgressRing(
                            current = todayTotal,
                            goal = dailyGoal
                        )
                    }
                }
            }

            item {
                Card(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PushPrimeColors.Surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Streak",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$streak days",
                                style = MaterialTheme.typography.bodyMedium,
                                color = PushPrimeColors.OnSurfaceVariant
                            )
                        }
                        Text(
                            text = "🔥",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
            }

            item {
                Card(
                    onClick = onNavigateToWorkout,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PushPrimeColors.Surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Recommended workout",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${recommendation.title} • ${recommendation.subtitle}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PushPrimeColors.OnSurfaceVariant
                                )
                            }
                            Button(onClick = onNavigateToWorkout) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Start"
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PushPrimeColors.Surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Last session",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = formatLastSession(lastSession),
                            style = MaterialTheme.typography.bodyMedium,
                            color = PushPrimeColors.OnSurfaceVariant
                        )
                    }
                }
            }

            item {
                Card(
                    onClick = onNavigateToNutrition,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PushPrimeColors.Surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Nutrition",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Daily targets & meal plans",
                                style = MaterialTheme.typography.bodyMedium,
                                color = PushPrimeColors.OnSurfaceVariant
                            )
                        }
                        Text(
                            text = "🥗",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
            }

            item {
                MiniMusicBar(
                    isConnected = isSpotifyConnected,
                    trackName = currentTrack?.name,
                    artistName = currentTrack?.artist?.name,
                    isPlaying = isPlaying,
                    onConnect = { spotifyHelper?.connect() },
                    onPlayPause = {
                        if (isPlaying) spotifyHelper?.pause() else spotifyHelper?.resume()
                    },
                    onNext = { spotifyHelper?.skipNext() },
                    onPrevious = { spotifyHelper?.skipPrevious() }
                )
            }
        }
    }
}

private data class Recommendation(
    val title: String,
    val subtitle: String
)

private fun formatLastSession(session: SessionEntity?): String {
    if (session == null) return "No sessions yet"
    val formatter = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
    val startTime = formatter.format(Date(session.startTime))
    val durationSeconds = session.totalSeconds ?: session.getDurationSeconds()
    val detailParts = mutableListOf<String>()
    if ((session.totalReps ?: 0) > 0) {
        detailParts.add("${session.totalReps} reps")
    }
    if (durationSeconds > 0) {
        detailParts.add(formatDuration(durationSeconds))
    }
    val details = if (detailParts.isEmpty()) "Logged activity" else detailParts.joinToString(" • ")
    return "$startTime • $details"
}

private fun formatDuration(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return if (hours > 0) {
        String.format("%dh %dm", hours, minutes)
    } else {
        String.format("%dm", minutes)
    }
}
