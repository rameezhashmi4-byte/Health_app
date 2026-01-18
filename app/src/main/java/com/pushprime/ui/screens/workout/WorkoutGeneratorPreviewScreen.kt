package com.pushprime.ui.screens.workout

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pushprime.model.GeneratedExercise
import com.pushprime.model.GeneratedWorkoutPlan
import com.pushprime.model.WorkoutBlock
import com.pushprime.model.WorkoutBlockType
import com.pushprime.model.WorkoutGoal
import com.pushprime.model.EquipmentOption
import com.pushprime.ui.theme.PushPrimeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutGeneratorPreviewScreen(
    planId: Long,
    onNavigateBack: () -> Unit,
    onStartSession: () -> Unit,
    viewModel: WorkoutGeneratorPreviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    WorkoutGeneratorPreviewContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onStartSession = onStartSession,
        onRegenerate = viewModel::regeneratePlan,
        onSavePlan = viewModel::savePlan
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutGeneratorPreviewContent(
    uiState: WorkoutGeneratorPreviewState,
    onNavigateBack: () -> Unit,
    onStartSession: () -> Unit,
    onRegenerate: () -> Unit,
    onSavePlan: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Generated Workout",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onRegenerate, enabled = !uiState.isRegenerating) {
                        Icon(Icons.Default.Refresh, contentDescription = "Regenerate")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                val plan = uiState.plan
                if (plan == null) {
                    Text(
                        text = uiState.errorMessage ?: "Loading plan...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    AnimatedContent(
                        targetState = plan,
                        transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
                        label = "plan_transition"
                    ) { targetPlan ->
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = targetPlan.title,
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Text(
                                text = "${targetPlan.totalDurationMinutes} min • ${targetPlan.goal.displayName} • ${targetPlan.equipment.displayName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            val plan = uiState.plan
            if (plan != null) {
                items(plan.blocks.size) { index ->
                    val block = plan.blocks[index]
                    WorkoutBlockCard(block = block)
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = onStartSession,
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) {
                            Text(
                                text = "Start Session",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        OutlinedButton(
                            onClick = onRegenerate,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            enabled = !uiState.isRegenerating
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.padding(horizontal = 6.dp))
                            Text(
                                text = if (uiState.isRegenerating) "Regenerating..." else "Regenerate",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        OutlinedButton(
                            onClick = onSavePlan,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            enabled = !uiState.isSaved
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.padding(horizontal = 6.dp))
                            Text(
                                text = if (uiState.isSaved) "Saved" else "Save Plan",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutBlockCard(block: WorkoutBlock) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = block.title,
                    style = MaterialTheme.typography.titleLarge
                )
                block.durationMinutes?.let {
                    Text(
                        text = "${it} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            block.exercises.forEach { exercise ->
                ExerciseRow(exercise = exercise)
            }
        }
    }
}

@Composable
private fun ExerciseRow(exercise: GeneratedExercise) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = exercise.name, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = formatExerciseTarget(exercise),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Rest ${exercise.restSeconds}s",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val note = exercise.notes ?: exercise.difficultyTag
            if (!note.isNullOrBlank()) {
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatExerciseTarget(exercise: GeneratedExercise): String {
    return when {
        exercise.seconds != null && exercise.sets != null -> "${exercise.sets} x ${exercise.seconds}s"
        exercise.seconds != null -> "${exercise.seconds}s"
        exercise.reps != null && exercise.sets != null -> "${exercise.sets} x ${exercise.reps}"
        exercise.reps != null -> "${exercise.reps} reps"
        else -> "Custom"
    }
}

@Preview(name = "Start Session - Light", showBackground = true)
@Preview(
    name = "Start Session - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun WorkoutGeneratorPreviewScreenPreview() {
    PushPrimeTheme {
        WorkoutGeneratorPreviewContent(
            uiState = WorkoutGeneratorPreviewState(
                plan = GeneratedWorkoutPlan(
                    id = 1,
                    title = "30-Min Fat Burn",
                    totalDurationMinutes = 30,
                    goal = WorkoutGoal.LOSE_FAT,
                    timeMinutes = 30,
                    equipment = EquipmentOption.HOME,
                    focus = null,
                    style = null,
                    blocks = listOf(
                        WorkoutBlock(
                            type = WorkoutBlockType.WARMUP,
                            title = "Warm-up",
                            durationMinutes = 5,
                            exercises = listOf(
                                GeneratedExercise(name = "Jumping jacks", seconds = 30, restSeconds = 15),
                                GeneratedExercise(name = "Bodyweight squats", reps = 12, sets = 2, restSeconds = 30)
                            )
                        ),
                        WorkoutBlock(
                            type = WorkoutBlockType.MAIN,
                            title = "Main Circuit",
                            durationMinutes = 20,
                            exercises = listOf(
                                GeneratedExercise(name = "Push-ups", reps = 10, sets = 3, restSeconds = 30),
                                GeneratedExercise(name = "Mountain climbers", seconds = 40, restSeconds = 20),
                                GeneratedExercise(name = "Reverse lunges", reps = 10, sets = 2, restSeconds = 30)
                            )
                        )
                    )
                ),
                isLoading = false,
                isSaved = false,
                isRegenerating = false
            ),
            onNavigateBack = {},
            onStartSession = {},
            onRegenerate = {},
            onSavePlan = {}
        )
    }
}
