package com.pushprime.ui.screens.nutrition

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pushprime.model.NutritionGoal
import com.pushprime.ui.theme.PushPrimeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionScreen(
    viewModel: NutritionViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "NUTRITION",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp
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
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Target Card
            item {
                Surface(
                    color = Color.Black,
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            "DAILY TARGET",
                            style = MaterialTheme.typography.labelLarge,
                            color = PushPrimeColors.GTAYellow,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "${uiState.targetCalories} kcal",
                            style = MaterialTheme.typography.displayMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MacroIndicator("P", "${uiState.targetMacros.protein}g", Color(0xFFE65A5F))
                            MacroIndicator("C", "${uiState.targetMacros.carbs}g", Color(0xFF276EF1))
                            MacroIndicator("F", "${uiState.targetMacros.fats}g", Color(0xFFFFD100))
                        }
                    }
                }
            }

            // Goal & Region
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("OBJECTIVE", fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelLarge)
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NutritionGoal.values().forEach { goal ->
                            GoalChip(
                                label = goal.name,
                                isSelected = uiState.settings.goal == goal,
                                onClick = { viewModel.updateGoal(goal) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    RegionSelector(
                        selectedRegion = uiState.settings.region,
                        onRegionSelected = { viewModel.updateRegion(it) }
                    )
                }
            }

            // Preferences
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("DIETARY PREFERENCES", fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelLarge)
                    
                    PreferenceToggle("Halal", uiState.settings.isHalal) { viewModel.toggleHalal(it) }
                    PreferenceToggle("Vegetarian", uiState.settings.isVeggie) { viewModel.toggleVeggie(it) }
                    PreferenceToggle("Budget Friendly", uiState.settings.isBudget) { viewModel.toggleBudget(it) }
                    PreferenceToggle("Restaurant Mode", uiState.settings.restaurantMode) { viewModel.toggleRestaurantMode(it) }
                }
            }

            // Meal Suggestions
            item {
                Text("MEAL SUGGESTIONS", fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelLarge)
            }

            items(uiState.mealSuggestions) { meal ->
                MealCard(meal)
            }
        }
    }
}

@Composable
fun MacroIndicator(label: String, value: String, color: Color) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Black)
        Text(value, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun GoalChip(label: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(48.dp)
            .background(if (isSelected) Color.Black else Color.LightGray)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (isSelected) Color.White else Color.Black,
            fontWeight = FontWeight.Black,
            fontSize = 12.sp
        )
    }
}

@Composable
fun RegionSelector(selectedRegion: String, onRegionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val regions = listOf("Global", "USA", "Europe", "Asia", "Middle East")

    Box {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            color = Color(0xFFF6F6F6),
            shape = RoundedCornerShape(0.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("REGION", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(selectedRegion, fontWeight = FontWeight.Black)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            regions.forEach { region ->
                DropdownMenuItem(
                    text = { Text(region) },
                    onClick = {
                        onRegionSelected(region)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun PreferenceToggle(label: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color(0xFFF6F6F6))
            .clickable { onCheckedChange(!isChecked) }
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontWeight = FontWeight.Bold)
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color.Black,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.LightGray
            )
        )
    }
}

@Composable
fun MealCard(meal: com.pushprime.model.MealSuggestion) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
        shape = RoundedCornerShape(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(meal.name, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                    Text("${meal.calories} kcal", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
                Icon(Icons.Default.Restaurant, contentDescription = null, tint = Color.LightGray)
            }
            
            if (meal.swapOptions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("SWAP OPTIONS:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color.Gray)
                Row(modifier = Modifier.horizontalScroll(androidx.compose.foundation.rememberScrollState())) {
                    meal.swapOptions.forEach { option ->
                        Text(
                            "• $option  ",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// Add horizontalScroll for swap options
@Composable
fun RowScope.MealTags(tags: List<String>) {
    // ...
}
