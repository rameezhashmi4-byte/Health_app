package com.pushprime.ui.screens.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pushprime.ui.components.AppTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionGoalsScreen(
    viewModel: NutritionGoalsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var caloriesText by remember(uiState.calorieGoal) { mutableStateOf(uiState.calorieGoal.toString()) }
    var proteinText by remember(uiState.proteinGoal) { mutableStateOf(uiState.proteinGoal.toString()) }
    val caloriesValue = caloriesText.toIntOrNull() ?: 0
    val proteinValue = proteinText.toIntOrNull() ?: 0
    val caloriesError = if (caloriesValue <= 0) "Enter calorie goal" else null
    val proteinError = if (proteinValue <= 0) "Enter protein goal" else null
    val isFormValid = caloriesValue > 0 && proteinValue > 0

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Nutrition Goals",
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
                "Daily Targets",
                style = MaterialTheme.typography.titleLarge
            )

            AppTextField(
                value = caloriesText,
                onValueChange = { caloriesText = it.filter { ch -> ch.isDigit() } },
                label = "Calorie goal",
                required = true,
                errorText = caloriesError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            AppTextField(
                value = proteinText,
                onValueChange = { proteinText = it.filter { ch -> ch.isDigit() } },
                label = "Protein goal (g)",
                required = true,
                errorText = proteinError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        viewModel.saveGoals(caloriesValue, proteinValue)
                        onNavigateBack()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = isFormValid
                ) {
                    Text(
                        text = "Save",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                OutlinedButton(onClick = onNavigateBack, modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
