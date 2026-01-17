package com.pushprime.ui.screens.sports

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pushprime.model.Intensity
import com.pushprime.model.SportType
import com.pushprime.model.SportsSession
import com.pushprime.ui.components.AppTextField
import com.pushprime.ui.theme.PushPrimeColors
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SportsSessionSummaryScreen(
    sportType: SportType,
    startTime: Long,
    endTime: Long,
    durationSeconds: Int,
    effortLevel: Intensity,
    intervalsEnabled: Boolean,
    warmupEnabled: Boolean,
    onSave: (SportsSession) -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val durationMinutes = (durationSeconds / 60).coerceAtLeast(1)
    val caloriesEstimate = estimateCalories(durationMinutes, effortLevel)
    var notes by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(3) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Session Summary",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PushPrimeColors.Surface
                )
            )
        },
        containerColor = Color.White,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PushPrimeColors.Surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = sportType.displayName,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Duration: ${formatDuration(durationSeconds)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Effort: ${effortLevel.displayName}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Calories: ~$caloriesEstimate kcal",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "How did it feel?",
                    style = MaterialTheme.typography.titleLarge
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..5).forEach { value ->
                        val selected = rating == value
                        Card(
                            modifier = Modifier
                                .clickable { rating = value }
                                .background(Color.Transparent),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) Color.Black else PushPrimeColors.Surface
                            )
                        ) {
                            Text(
                                text = value.toString(),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                color = if (selected) Color.White else Color.Black,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            AppTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Notes (optional)",
                singleLine = false,
                fieldModifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    onSave(
                        SportsSession(
                            sportType = sportType,
                            startTime = startTime,
                            endTime = endTime,
                            durationMinutes = durationMinutes,
                            effortLevel = effortLevel,
                            intervalsEnabled = intervalsEnabled,
                            warmupEnabled = warmupEnabled,
                            notes = notes,
                            rating = rating,
                            caloriesEstimate = caloriesEstimate
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
            ) {
                Text(
                    text = "Save Session",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Button(
                onClick = onDiscard,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PushPrimeColors.Surface,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "Discard",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

private fun estimateCalories(durationMinutes: Int, effort: Intensity): Int {
    val base = 6.0
    val multiplier = when (effort) {
        Intensity.LOW -> 1.0
        Intensity.MEDIUM -> 1.3
        Intensity.HIGH -> 1.6
    }
    return (durationMinutes * base * multiplier).roundToInt()
}

private fun formatDuration(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%d:%02d", mins, secs)
}
