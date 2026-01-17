package com.pushprime.ui.screens.workout

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pushprime.model.EquipmentOption
import com.pushprime.model.TrainingStyle
import com.pushprime.model.WorkoutFocus
import com.pushprime.model.WorkoutGoal
import com.pushprime.ui.theme.PushPrimeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutGeneratorSetupScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPreview: (Long) -> Unit,
    viewModel: WorkoutGeneratorSetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.generatedPlanId) {
        val planId = uiState.generatedPlanId ?: return@LaunchedEffect
        viewModel.consumeGeneratedPlanId()
        onNavigateToPreview(planId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Workout Generator", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onNavigateBack) {
                        androidx.compose.material3.Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PushPrimeColors.Surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tell RAMBOOST what you've got — we'll build the session.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PushPrimeColors.OnSurfaceVariant
                    )
                }
            }

            item {
                GeneratorSectionCard(
                    title = "Goal",
                    subtitle = "Choose your primary outcome"
                ) {
                    ChipRow(
                        options = WorkoutGoal.values().toList(),
                        selected = uiState.goal,
                        label = { it.displayName },
                        onSelected = viewModel::selectGoal
                    )
                }
            }

            item {
                GeneratorSectionCard(
                    title = "Time Available",
                    subtitle = "Pick a duration"
                ) {
                    ChipRow(
                        options = listOf(10, 20, 30, 45, 60),
                        selected = uiState.timeMinutes,
                        label = { "$it min" },
                        onSelected = viewModel::selectTime
                    )
                }
            }

            item {
                GeneratorSectionCard(
                    title = "Equipment",
                    subtitle = "Match your setup"
                ) {
                    ChipRow(
                        options = EquipmentOption.values().toList(),
                        selected = uiState.equipment,
                        label = { it.displayName },
                        onSelected = viewModel::selectEquipment
                    )
                }
            }

            item {
                GeneratorSectionCard(
                    title = "Focus (optional)",
                    subtitle = "Dial in the emphasis"
                ) {
                    ChipRow(
                        options = WorkoutFocus.values().toList(),
                        selected = uiState.focus,
                        label = { it.displayName },
                        onSelected = viewModel::selectFocus
                    )
                }
            }

            item {
                GeneratorSectionCard(
                    title = "Training Style (optional)",
                    subtitle = "Give it a vibe"
                ) {
                    ChipRow(
                        options = TrainingStyle.values().toList(),
                        selected = uiState.style,
                        label = { it.displayName },
                        onSelected = viewModel::selectStyle
                    )
                }
            }

            item {
                AnimatedVisibility(visible = uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            item {
                Button(
                    onClick = { viewModel.generatePlan() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = uiState.canGenerate && !uiState.isGenerating
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 6.dp))
                    Text(text = if (uiState.isGenerating) "Generating..." else "Generate Workout")
                }
            }
        }
    }
}

@Composable
private fun <T> ChipRow(
    options: List<T>,
    selected: T?,
    label: (T) -> String,
    onSelected: (T) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(options.size) { index ->
            val option = options[index]
            val isSelected = option == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelected(option) },
                label = { Text(label(option)) }
            )
        }
    }
}

@Composable
private fun GeneratorSectionCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PushPrimeColors.Surface),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = PushPrimeColors.OnSurfaceVariant)
            content()
        }
    }
}
