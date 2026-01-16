package com.pushprime.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pushprime.ui.components.FeedCard
import com.pushprime.ui.components.StoryType
import com.pushprime.ui.components.StoriesRow
import com.pushprime.data.LocalStore
import com.pushprime.ui.theme.PushPrimeColors
import java.text.SimpleDateFormat
import java.util.*

/**
 * Home Screen (Feed)
 * Instagram-like feed with stories row and cards
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    localStore: LocalStore,
    onNavigateToWorkout: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToCompete: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToPhotoVault: () -> Unit,
    onNavigateToSports: () -> Unit,
    onNavigateToTodayPlan: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Optimize state collection - use initial value to prevent null issues
    val user by localStore.user.collectAsState(initial = null)
    val sessions by localStore.sessions.collectAsState(initial = emptyList())
    
    // Cache expensive calculations
    var todayTotal by remember { mutableStateOf(0) }
    var streak by remember { mutableStateOf(0) }
    var dailyGoal by remember { mutableStateOf(100) }
    var recentSession by remember { mutableStateOf<com.pushprime.model.Session?>(null) }
    
    // Calculate values in LaunchedEffect to avoid blocking UI
    LaunchedEffect(sessions, user) {
        try {
            todayTotal = localStore.getTodayTotalPushups()
            streak = localStore.getStreak()
            dailyGoal = user?.dailyGoal ?: 100
            recentSession = sessions.firstOrNull()
        } catch (e: Exception) {
            // Silently handle errors to prevent crashes
        }
    }
    
    // Cache date formatting
    val today = remember {
        SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()).format(Date())
    }
    
    val dateFormatter = remember {
        SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "PushPrime",
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
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stories Row
            item {
                StoriesRow(
                    onStoryClick = { storyType ->
                        when (storyType) {
                            StoryType.QUICK_START -> onNavigateToWorkout()
                            StoryType.TODAY_PLAN -> onNavigateToTodayPlan()
                            StoryType.SPORTS -> onNavigateToSports()
                            StoryType.PROGRESS -> onNavigateToProgress()
                            StoryType.BEFORE_AFTER -> onNavigateToPhotoVault()
                        }
                    }
                )
            }
            
            // Today's Goal Card
            item {
                FeedCard(
                    title = "Today's Goal",
                    subtitle = today,
                    icon = Icons.Default.TrackChanges,
                    onClick = onNavigateToProgress
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "$todayTotal / $dailyGoal",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = PushPrimeColors.Primary
                            )
                            Text(
                                text = "push-ups today",
                                style = MaterialTheme.typography.bodyMedium,
                                color = PushPrimeColors.OnSurfaceVariant
                            )
                        }
                        CircularProgressIndicator(
                            progress = (todayTotal.toFloat() / dailyGoal).coerceIn(0f, 1f),
                            modifier = Modifier.size(48.dp),
                            color = PushPrimeColors.Primary
                        )
                    }
                }
            }
            
            // Streak Card
            item {
                FeedCard(
                    title = "🔥 Streak",
                    subtitle = "Keep it going!",
                    emoji = "🔥",
                    onClick = onNavigateToProgress
                ) {
                    Text(
                        text = "$streak days",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = PushPrimeColors.Warning
                    )
                }
            }
            
            // Suggested Workout Card
            item {
                FeedCard(
                    title = "Suggested Workout",
                    subtitle = "Leg Day + Football conditioning (20 min)",
                    icon = Icons.Default.FitnessCenter,
                    onClick = onNavigateToWorkout
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WorkoutTag("Legs")
                        WorkoutTag("Cardio")
                        WorkoutTag("20 min")
                    }
                }
            }
            
            // Recent Session Card
            item {
                val currentSession = recentSession
                FeedCard(
                    title = "Recent Session",
                    subtitle = remember(currentSession) {
                        currentSession?.let {
                            try {
                                dateFormatter.format(Date(it.timestamp))
                            } catch (e: Exception) {
                                "Recent session"
                            }
                        } ?: "No sessions yet"
                    },
                    icon = Icons.Default.History,
                    onClick = onNavigateToProgress
                ) {
                    currentSession?.let { session ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${session.pushups} push-ups",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = formatTime(session.workoutTime),
                                style = MaterialTheme.typography.bodyMedium,
                                color = PushPrimeColors.OnSurfaceVariant
                            )
                        }
                    } ?: run {
                        Text(
                            text = "Start your first workout!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PushPrimeColors.OnSurfaceVariant
                        )
                    }
                }
            }
            
            // Music Status Card (if Spotify connected)
            item {
                FeedCard(
                    title = "Music",
                    subtitle = "Not connected",
                    icon = Icons.Default.MusicNote,
                    onClick = onNavigateToProfile
                ) {
                    Text(
                        text = "Connect Spotify to play music during workouts",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PushPrimeColors.OnSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun WorkoutTag(text: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = PushPrimeColors.Primary.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = PushPrimeColors.Primary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
