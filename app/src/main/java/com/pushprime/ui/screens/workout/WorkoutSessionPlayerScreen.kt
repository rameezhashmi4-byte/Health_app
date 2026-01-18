package com.pushprime.ui.screens.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pushprime.data.AppDatabase
import com.pushprime.data.WorkoutPlanRepository
import com.pushprime.data.WorkoutSessionRepository
import com.pushprime.model.GeneratedWorkoutPlan
import com.pushprime.model.WorkoutExerciseSession
import com.pushprime.model.WorkoutSession
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSessionPlayerScreen(
    planId: Long,
    currentUserId: String?,
    onNavigateBack: () -> Unit,
    onFinishSession: (Long) -> Unit,
    onNavigateToWorkoutPlayer: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val database = remember(context) { AppDatabase.getDatabase(context) }
    val workoutPlanRepository = remember { WorkoutPlanRepository(database.workoutPlanDao()) }
    val workoutSessionRepository = remember { WorkoutSessionRepository(database.workoutSessionDao()) }

    var isLoading by remember { mutableStateOf(true) }
    var plan by remember { mutableStateOf<GeneratedWorkoutPlan?>(null) }
    var existingSession by remember { mutableStateOf<WorkoutSession?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(planId, currentUserId) {
        if (currentUserId == null) {
            errorMessage = "User not logged in"
            isLoading = false
            return@LaunchedEffect
        }

        try {
            // Check if there's already an active session for this plan
            existingSession = workoutSessionRepository.getActiveSessionForPlan(currentUserId, planId)

            if (existingSession != null) {
                // Resume existing session
                onNavigateToWorkoutPlayer(existingSession!!.sessionId)
                return@LaunchedEffect
            }

            // Load the plan to create a new session
            plan = workoutPlanRepository.getPlan(planId)
            if (plan == null) {
                errorMessage = "Plan not found"
            }
        } catch (e: Exception) {
            errorMessage = "Failed to load plan: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    fun createSessionFromPlan() {
        if (plan == null || currentUserId == null) return

        coroutineScope.launch {
            try {
                val exercises = plan!!.blocks.flatMap { block ->
                    block.exercises.map { exercise ->
                        WorkoutExerciseSession(
                            name = exercise.name,
                            targetReps = exercise.reps,
                            targetSeconds = exercise.seconds,
                            restSeconds = exercise.restSeconds,
                            cue = "", // Could be enhanced with AI-generated cues
                            blockType = block.type,
                            intensityTag = exercise.intensityTag
                        )
                    }
                }

                val session = WorkoutSession.create(
                    userId = currentUserId,
                    planId = planId,
                    exercises = exercises
                )

                workoutSessionRepository.createSession(session)
                onNavigateToWorkoutPlayer(session.sessionId)
            } catch (e: Exception) {
                errorMessage = "Failed to create session: ${e.message}"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Start Workout Session") },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onNavigateBack) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Preparing your workout...")
                    }
                }
                errorMessage != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = errorMessage ?: "Unknown error",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onNavigateBack) {
                            Text("Go Back")
                        }
                    }
                }
                existingSession != null -> {
                    // This should not be reached due to the LaunchedEffect navigation
                    Text("Resuming existing session...")
                }
                plan != null -> {
                    Text(
                        text = plan!!.title,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "${plan!!.totalDurationMinutes} min • ${plan!!.goal.displayName} • ${plan!!.equipment.displayName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${plan!!.blocks.sumOf { it.exercises.size }} exercises",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { createSessionFromPlan() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Start Session",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}
