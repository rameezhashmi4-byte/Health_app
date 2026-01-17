package com.pushprime.ui.screens.nutrition

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import com.pushprime.ui.components.AppCard
import com.pushprime.ui.components.AppPrimaryButton
import com.pushprime.ui.components.AppSecondaryButton
import com.pushprime.ui.components.AppTextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pushprime.ui.components.AppTextField
import androidx.hilt.navigation.compose.hiltViewModel
import com.pushprime.model.NutritionEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionScreen(
    viewModel: NutritionViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToAddMeal: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var quickCalories by remember { mutableStateOf<QuickAddState?>(null) }
    var quickProtein by remember { mutableStateOf<QuickAddState?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Nutrition",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                NutritionTotalsCard(
                    totalCalories = uiState.totalCalories,
                    calorieGoal = uiState.calorieGoal,
                    totalProtein = uiState.totalProtein,
                    proteinGoal = uiState.proteinGoal
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    AppPrimaryButton(
                        text = "Add Meal",
                        onClick = onNavigateToAddMeal,
                        modifier = Modifier.weight(1f)
                    )
                    AppSecondaryButton(
                        text = "Quick Cals",
                        onClick = { quickCalories = QuickAddState("Quick Add Calories") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                AppSecondaryButton(
                    text = "Quick Add Protein",
                    onClick = { quickProtein = QuickAddState("Quick Add Protein") }
                )
            }

            item {
                Text(
                    text = "7-Day Calories",
                    style = MaterialTheme.typography.titleLarge
                )
                NutritionMiniChart(summaries = uiState.last7Days)
            }

            item {
                Text(
                    text = "Today Logs",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            if (uiState.entries.isEmpty()) {
                item {
                    Text("No meals logged yet.", color = Color.Gray)
                }
            } else {
                items(uiState.entries) { entry ->
                    NutritionEntryCard(entry = entry)
                }
            }
        }
    }

    quickCalories?.let { state ->
        QuickAddDialog(
            title = state.title,
            onDismiss = { quickCalories = null },
            onSave = { value ->
                viewModel.quickAddCalories(value)
                quickCalories = null
            }
        )
    }

    quickProtein?.let { state ->
        QuickAddDialog(
            title = state.title,
            onDismiss = { quickProtein = null },
            onSave = { value ->
                viewModel.quickAddProtein(value)
                quickProtein = null
            }
        )
    }
}

data class QuickAddState(val title: String)

@Composable
fun NutritionTotalsCard(
    totalCalories: Int,
    calorieGoal: Int,
    totalProtein: Int,
    proteinGoal: Int
) {
    AppCard(
        containerColor = Color.Black,
        contentColor = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Text(
                "Today Calories",
                color = Color(0xFFFFD100),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "$totalCalories / $calorieGoal",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Today Protein",
                color = Color(0xFFFFD100),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "${totalProtein}g / ${proteinGoal}g",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
fun NutritionMiniChart(summaries: List<NutritionDaySummary>) {
    val maxValue = summaries.maxOfOrNull { it.calories }.takeIf { it != null && it > 0 } ?: 1
    Row(
        modifier = Modifier.fillMaxWidth().height(72.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        summaries.forEach { day ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height((56.dp * (day.calories.toFloat() / maxValue)).coerceAtLeast(6.dp))
                        .background(
                            if (day.calories > 0) Color.Black else Color.LightGray,
                            RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                        )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(day.date.takeLast(2), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun NutritionEntryCard(entry: NutritionEntry) {
    AppCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.name.ifBlank { entry.mealType.lowercase().replaceFirstChar { it.uppercase() } },
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${entry.calories ?: 0} kcal • ${entry.proteinGrams ?: 0}g protein",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Icon(Icons.Default.Restaurant, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun QuickAddDialog(
    title: String,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var valueText by remember { mutableStateOf("") }
    val value = valueText.toIntOrNull() ?: 0
    val valueError = if (value <= 0) "Enter a value" else null
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            AppTextField(
                value = valueText,
                onValueChange = { valueText = it.filter { ch -> ch.isDigit() } },
                label = "Value",
                required = true,
                errorText = valueError,
                placeholder = "Enter value",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            val isFormValid = value > 0
            AppTextButton(
                text = "Save",
                onClick = { onSave(value) },
                enabled = isFormValid
            )
        },
        dismissButton = {
            AppTextButton(onClick = onDismiss, text = "Cancel")
        }
    )
}
