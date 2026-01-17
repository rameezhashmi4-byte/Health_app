package com.pushprime.ui.screens.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pushprime.model.MealType
import com.pushprime.ui.components.AppTextField
import com.pushprime.ui.validation.rememberFormValidationState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealScreen(
    viewModel: AddMealViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var caloriesText by remember { mutableStateOf("") }
    var proteinText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var mealType by remember { mutableStateOf(MealType.BREAKFAST) }
    val validation = rememberFormValidationState()

    val calories = caloriesText.toIntOrNull()
    val protein = proteinText.toIntOrNull()
    val isValid = (calories ?: 0) > 0 || (protein ?: 0) > 0
    val showMacroError = validation.shouldShowError("macros") && !isValid

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Add Meal",
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
            AppTextField(
                value = name,
                onValueChange = { name = it },
                label = "Meal name",
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppTextField(
                    value = caloriesText,
                    onValueChange = {
                        caloriesText = it.filter { ch -> ch.isDigit() }
                        validation.markTouched("macros")
                    },
                    label = "Calories",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                AppTextField(
                    value = proteinText,
                    onValueChange = {
                        proteinText = it.filter { ch -> ch.isDigit() }
                        validation.markTouched("macros")
                    },
                    label = "Protein (g)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = "Meal type",
                style = MaterialTheme.typography.titleLarge
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MealType.values().forEach { type ->
                    OutlinedButton(
                        onClick = { mealType = type },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = type.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            AppTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Notes (optional)",
                modifier = Modifier.fillMaxWidth()
            )

            if (showMacroError) {
                Text(
                    text = "Please enter calories or protein.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        validation.markSubmitAttempt()
                        if (!isValid) {
                            return@Button
                        }
                        viewModel.saveMeal(
                            mealType = mealType,
                            name = name,
                            calories = calories,
                            protein = protein,
                            notes = notes
                        )
                        onNavigateBack()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = isValid
                ) {
                    Text(
                        text = "Save",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
