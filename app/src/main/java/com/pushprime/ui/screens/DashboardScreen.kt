package com.pushprime.ui.screens

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pushprime.data.ExerciseRepository
import com.pushprime.data.LocalStore
import com.pushprime.model.ExerciseLog
import com.pushprime.model.ExerciseType
import com.pushprime.model.Session
import com.pushprime.ui.components.DailyExerciseSummary
import com.pushprime.ui.components.ExerciseLogger
import com.pushprime.ui.components.ExerciseSelector
import com.pushprime.ui.components.PushUpCounter
import com.pushprime.ui.components.ProgressRing
import com.pushprime.ui.theme.PushPrimeColors
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Dashboard Screen
 * Multi-exercise tracking with Room database
 * Supports: Push-ups, Sit-ups, Squats, Pull-ups, Plank, etc.
 */
@Composable
fun DashboardScreen(
    localStore: LocalStore,
    onNavigateToCoaching: () -> Unit,
    onNavigateToCompete: () -> Unit,
    onNavigateToGroup: () -> Unit,
    onNavigateToMotivation: () -> Unit,
    onNavigateToMetrics: () -> Unit = {}
) {
    val context = LocalContext.current
    val exerciseRepository = remember { ExerciseRepository(context) }
    
    // Exercise selection state
    var selectedExercise by remember { mutableStateOf<ExerciseType?>(null) }
    var dailySummary by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    
    // Legacy push-up counter state (for backward compatibility)
    var pushupCount by remember { mutableStateOf(0) }
    var isActive by remember { mutableStateOf(false) }
    var elapsedTime by remember { mutableStateOf(0L) }
    var timerJob by remember { mutableStateOf<Job?>(null) }
    val coroutineScope = rememberCoroutineScope()
    
    val user by localStore.user.collectAsState()
    val sessions by localStore.sessions.collectAsState()
    val todayTotal = remember(sessions) { localStore.getTodayTotalPushups() }
    val todayTime = remember(sessions) { localStore.getTodayTotalTime() }
    val streak = remember(sessions) { localStore.getStreak() }
    val dailyGoal = user?.dailyGoal ?: 0
    
    // Load daily summary
    LaunchedEffect(Unit) {
        dailySummary = exerciseRepository.getDailySummary()
    }
    
    fun startTimer() {
        timerJob?.cancel()
        timerJob = coroutineScope.launch {
            while (isActive) {
                delay(1000)
                elapsedTime++
            }
        }
    }
    
    fun stopTimer() {
        timerJob?.cancel()
        if (pushupCount > 0 && elapsedTime > 0) {
            val session = Session(
                id = System.currentTimeMillis().toString(),
                username = user?.username ?: "User",
                pushups = pushupCount,
                workoutTime = elapsedTime.toInt(),
                timestamp = System.currentTimeMillis(),
                country = user?.country ?: "US",
                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            )
            localStore.saveSession(session)
        }
    }
    
    // Handle exercise log save
    fun onExerciseLogged(repsOrDuration: Int, intensity: Int) {
        if (selectedExercise != null) {
            coroutineScope.launch {
                val exerciseLog = ExerciseLog(
                    exerciseName = selectedExercise!!.displayName,
                    repsOrDuration = repsOrDuration,
                    intensity = intensity,
                    workoutDuration = 0 // Can be enhanced later
                )
                exerciseRepository.insertLog(exerciseLog)
                // Refresh summary
                dailySummary = exerciseRepository.getDailySummary()
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PushPrimeColors.Background)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PushPrime",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = PushPrimeColors.OnSurface
            )
            Row {
                IconButton(onClick = onNavigateToCoaching) {
                    Icon(Icons.Default.Person, contentDescription = "Coaching")
                }
                IconButton(onClick = onNavigateToMotivation) {
                    Icon(Icons.Default.Newspaper, contentDescription = "Motivation")
                }
            }
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Streak Card
            item {
                StreakCard(streak = streak)
            }
            
            // Daily Exercise Summary
            item {
                DailyExerciseSummary(summary = dailySummary)
            }
            
            // Exercise Selector
            item {
                ExerciseSelector(
                    selectedExercise = selectedExercise,
                    onExerciseSelected = { exercise ->
                        selectedExercise = exercise
                    }
                )
            }
            
            // Exercise Logger (shown when exercise is selected)
            if (selectedExercise != null) {
                item {
                    ExerciseLogger(
                        exercise = selectedExercise!!,
                        onLogSaved = { repsOrDuration, intensity ->
                            onExerciseLogged(repsOrDuration, intensity)
                        }
                    )
                }
            }
            
            // Legacy Push-Up Counter (for backward compatibility)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = PushPrimeColors.Surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Quick Push-up Counter",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PushPrimeColors.OnSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        PushUpCounter(
                            pushupCount = pushupCount,
                            isActive = isActive,
                            elapsedTime = elapsedTime,
                            onIncrement = { if (isActive) pushupCount++ },
                            onStart = {
                                isActive = true
                                elapsedTime = 0L
                                startTimer()
                            },
                            onStop = {
                                isActive = false
                                stopTimer()
                            },
                            onReset = {
                                pushupCount = 0
                                elapsedTime = 0L
                                isActive = false
                                timerJob?.cancel()
                            }
                        )
                    }
                }
            }
            
            // Progress Ring and Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProgressRing(
                        current = todayTotal + pushupCount,
                        goal = dailyGoal,
                        modifier = Modifier.weight(1f)
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = formatTime((todayTime + elapsedTime).toInt()),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = PushPrimeColors.OnSurface
                        )
                        Text(
                            text = "Workout Time",
                            style = MaterialTheme.typography.bodySmall,
                            color = PushPrimeColors.OnSurfaceVariant
                        )
                    }
                }
            }
            
            // Navigation Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    NavigationCard(
                        title = "Metrics",
                        icon = Icons.Default.TrendingUp,
                        onClick = onNavigateToMetrics,
                        modifier = Modifier.weight(1f)
                    )
                    NavigationCard(
                        title = "Compete",
                        icon = Icons.Default.EmojiEvents,
                        onClick = onNavigateToCompete,
                        modifier = Modifier.weight(1f)
                    )
                    NavigationCard(
                        title = "Group",
                        icon = Icons.Default.Group,
                        onClick = onNavigateToGroup,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun StreakCard(streak: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = PushPrimeColors.Warning.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🔥 Streak: $streak days!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = PushPrimeColors.OnSurface
                )
                Text(
                    text = "Keep it going!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PushPrimeColors.OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun NavigationCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = PushPrimeColors.Surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = PushPrimeColors.Primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = PushPrimeColors.OnSurface
            )
        }
    }
}

fun formatTime(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format("%d:%02d", minutes, secs)
    }
}
