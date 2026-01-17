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
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
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
                    Button(
                        onClick = onNavigateToAddMeal,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Meal", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { quickCalories = QuickAddState("Quick Add Calories") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Quick Add Calories")
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = { quickProtein = QuickAddState("Quick Add Protein") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.LocalDining, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Quick Add Protein")
                }
            }

            item {
                Text("7-Day Calories", fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelLarge)
                NutritionMiniChart(summaries = uiState.last7Days)
            }

            item {
                Text("Today Logs", fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelLarge)
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
    Surface(
        color = Color.Black,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Today Calories",
                color = Color(0xFFFFD100),
                fontWeight = FontWeight.Bold
            )
            Text(
                "$totalCalories / $calorieGoal",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Today Protein",
                color = Color(0xFFFFD100),
                fontWeight = FontWeight.Bold
            )
            Text(
                "${totalProtein}g / ${proteinGoal}g",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
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
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE6E6E6)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.name.ifBlank { entry.mealType.lowercase().replaceFirstChar { it.uppercase() } },
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "${entry.calories ?: 0} kcal • ${entry.proteinGrams ?: 0}g protein",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Icon(Icons.Default.Restaurant, contentDescription = null, tint = Color.LightGray)
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            TextField(
                value = valueText,
                onValueChange = { valueText = it.filter { ch -> ch.isDigit() } },
                placeholder = { Text("Enter value") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            TextButton(onClick = {
                val value = valueText.toIntOrNull() ?: 0
                if (value > 0) onSave(value)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
