package com.pushprime.ui.screens.quick_session

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pushprime.model.QuickSessionTemplates
import com.pushprime.ui.theme.PushPrimeColors
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun QuickSessionPlayerScreen(
    templateId: String,
    onNavigateBack: () -> Unit,
    onComplete: () -> Unit
) {
    val template = remember(templateId) { QuickSessionTemplates.byId(templateId) }
    val onCompleteState = rememberUpdatedState(onComplete)

    if (template == null) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Quick session not found", color = Color.Gray)
            }
        }
        return
    }

    val totalDuration = template.rounds * (template.workSeconds + template.restSeconds)
    var totalRemaining by rememberSaveable { mutableIntStateOf(totalDuration) }
    var segmentRemaining by rememberSaveable { mutableIntStateOf(template.workSeconds) }
    var roundIndex by rememberSaveable { mutableIntStateOf(1) }
    var isWorkPhase by rememberSaveable { mutableStateOf(true) }
    var isPaused by rememberSaveable { mutableStateOf(false) }
    var showEndConfirm by remember { mutableStateOf(false) }
    var completed by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(isPaused, templateId) {
        if (completed) return@LaunchedEffect
        while (!isPaused && totalRemaining > 0) {
            delay(1000)
            totalRemaining -= 1
            segmentRemaining -= 1

            if (segmentRemaining <= 0) {
                if (isWorkPhase) {
                    isWorkPhase = false
                    segmentRemaining = template.restSeconds
                } else {
                    isWorkPhase = true
                    roundIndex = (roundIndex + 1).coerceAtMost(template.rounds)
                    segmentRemaining = template.workSeconds
                }
            }
        }

        if (totalRemaining <= 0 && !completed) {
            completed = true
            onCompleteState.value.invoke()
        }
    }

    val currentExerciseIndex = (roundIndex - 1).coerceAtLeast(0) % template.exercises.size
    val currentExercise = template.exercises[currentExerciseIndex]
    val nextRoundIndex = roundIndex + 1
    val nextExercise = if (nextRoundIndex <= template.rounds) {
        template.exercises[(nextRoundIndex - 1) % template.exercises.size]
    } else {
        "Finish strong"
    }

    val workDisplay = if (isWorkPhase) segmentRemaining else 0
    val restDisplay = if (!isWorkPhase) segmentRemaining else template.restSeconds

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { showEndConfirm = true }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = formatSeconds(totalRemaining),
                style = MaterialTheme.typography.displayLarge
            )

            Text(
                text = "Round ${roundIndex} / ${template.rounds}",
                color = PushPrimeColors.OnSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )

            AnimatedContent(
                targetState = currentExercise,
                transitionSpec = { fadeIn() togetherWith fadeOut() }
            ) { target ->
                Text(
                    text = target,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SegmentCard(label = "Work", value = formatSeconds(workDisplay), Modifier.weight(1f))
                SegmentCard(label = "Rest", value = formatSeconds(restDisplay), Modifier.weight(1f))
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = PushPrimeColors.UberGrey)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Next", color = PushPrimeColors.OnSurfaceVariant)
                        Text(
                            text = nextExercise,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { isPaused = !isPaused },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isPaused) "Resume" else "Pause",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Button(
                    onClick = {
                        val skipBy = segmentRemaining.coerceAtLeast(0)
                        totalRemaining = (totalRemaining - skipBy).coerceAtLeast(0)
                        if (totalRemaining <= 0 && !completed) {
                            completed = true
                            onCompleteState.value.invoke()
                        } else if (isWorkPhase) {
                            isWorkPhase = false
                            segmentRemaining = template.restSeconds
                        } else {
                            isWorkPhase = true
                            roundIndex = (roundIndex + 1).coerceAtMost(template.rounds)
                            segmentRemaining = template.workSeconds
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = PushPrimeColors.PrimaryLight),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Skip",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Button(
                onClick = { showEndConfirm = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFDECEC)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "End session early",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFFB3261E)
                )
            }
        }
    }

    if (showEndConfirm) {
        AlertDialog(
            onDismissRequest = { showEndConfirm = false },
            title = { Text("End session early?") },
            text = { Text("Your quick session won't be saved.") },
            confirmButton = {
                Button(
                    onClick = {
                        showEndConfirm = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB3261E))
                ) {
                    Text(
                        text = "End",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndConfirm = false }) {
                    Text("Keep going")
                }
            }
        )
    }
}

@Composable
private fun SegmentCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = PushPrimeColors.UberGrey),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, color = PushPrimeColors.OnSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

private fun formatSeconds(totalSeconds: Int): String {
    val minutes = (totalSeconds / 60).coerceAtLeast(0)
    val seconds = (totalSeconds % 60).coerceAtLeast(0)
    return String.format("%02d:%02d", minutes, seconds)
}
