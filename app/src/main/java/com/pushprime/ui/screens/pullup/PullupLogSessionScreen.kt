package com.pushprime.ui.screens.pullup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pushprime.ui.components.AppTextField
import com.pushprime.ui.validation.rememberFormValidationState
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullupLogSessionScreen(
    viewModel: PullupLogSessionViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val repsInputs = remember { mutableStateListOf("8", "6", "5") }
    var addedWeightText by remember { mutableStateOf("") }
    var restText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val validation = rememberFormValidationState()

    val repsBySet = repsInputs.map { it.toIntOrNull() ?: 0 }
    val totalReps = repsBySet.sum()
    val addedWeightKg = addedWeightText.toDoubleOrNull()
    val volumeScore = calculateVolumeScore(totalReps, addedWeightKg)

    val hasValidSet = repsBySet.any { it > 0 }
    val showSetError = validation.shouldShowError("sets") && !hasValidSet
    val isFormValid = hasValidSet

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Log Pull-Up Session",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Sets",
                style = MaterialTheme.typography.titleLarge
            )
            repsInputs.forEachIndexed { index, value ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    AppTextField(
                        value = value,
                        onValueChange = {
                            repsInputs[index] = it.filter { ch -> ch.isDigit() }
                            validation.markTouched("sets")
                        },
                        label = "Set ${index + 1}",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { if (repsInputs.size > 1) repsInputs.removeAt(index) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove set")
                    }
                }
            }
            OutlinedButton(onClick = { repsInputs.add("") }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Set",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            AppTextField(
                value = addedWeightText,
                onValueChange = { addedWeightText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                label = "Added weight (kg)",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            AppTextField(
                value = restText,
                onValueChange = { restText = it.filter { ch -> ch.isDigit() } },
                label = "Rest time (seconds)",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            AppTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Notes (optional)",
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Total reps: $totalReps",
                style = MaterialTheme.typography.bodyLarge
            )
            Text("Volume score: $volumeScore", color = Color.Gray)

            if (showSetError) {
                Text(
                    text = "Add at least one set with reps.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    validation.markSubmitAttempt()
                    if (!hasValidSet) {
                        return@Button
                    }
                    viewModel.saveSession(
                        repsBySet = repsBySet,
                        addedWeightKg = addedWeightKg,
                        restSeconds = restText.toIntOrNull(),
                        notes = notes
                    )
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isFormValid
            ) {
                Text(
                    text = "Save Session",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

private fun calculateVolumeScore(totalReps: Int, addedWeightKg: Double?): Int {
    val weightFactor = ((addedWeightKg ?: 0.0) / 20.0).coerceAtLeast(0.0)
    return (totalReps * (1.0 + weightFactor)).roundToInt()
}
