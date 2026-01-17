package com.pushprime.ui.screens.workout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pushprime.model.ActivityType
import com.pushprime.navigation.Screen
import com.pushprime.ui.components.FeedCard
import com.pushprime.ui.theme.PushPrimeColors

/**
 * Workout Screen
 * Main workout hub with Gym/Sports toggle
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    onNavigateToWorkoutPlayer: (Long?) -> Unit,
    onNavigateToSports: () -> Unit,
    onNavigateToExerciseLibrary: () -> Unit,
    onNavigateToWorkoutGenerator: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedActivityType by remember { mutableStateOf<ActivityType>(ActivityType.GYM) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Workout",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PushPrimeColors.Surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Activity Type Toggle
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        selected = selectedActivityType == ActivityType.GYM,
                        onClick = { selectedActivityType = ActivityType.GYM },
                        label = { Text("Gym") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedActivityType == ActivityType.SPORT,
                        onClick = { selectedActivityType = ActivityType.SPORT },
                        label = { Text("Sports") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Sports,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Quick Start Card
            item {
                FeedCard(
                    title = "Quick Start",
                    subtitle = "Start a new workout session",
                    icon = Icons.Default.PlayArrow,
                    onClick = { onNavigateToWorkoutPlayer(null) }
                ) {
                    Text(
                        text = "Begin your workout now",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PushPrimeColors.OnSurfaceVariant
                    )
                }
            }

            item {
                FeedCard(
                    title = "Workout Generator",
                    subtitle = "Auto-build a session in seconds",
                    icon = Icons.Default.AutoAwesome,
                    onClick = onNavigateToWorkoutGenerator
                ) {
                    Text(
                        text = "Tell RAMBOOST your time and gear",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PushPrimeColors.OnSurfaceVariant
                    )
                }
            }
            
            // Gym Mode Options
            if (selectedActivityType == ActivityType.GYM) {
                item {
                    FeedCard(
                        title = "Exercise Library",
                        subtitle = "Browse exercises by muscle group",
                        icon = Icons.Default.List,
                        onClick = onNavigateToExerciseLibrary
                    )
                }
            }
            
            // Sports Mode Options
            if (selectedActivityType == ActivityType.SPORT) {
                item {
                    FeedCard(
                        title = "Select Sport",
                        subtitle = "Football, Cricket, Rugby, and more",
                        icon = Icons.Default.Sports,
                        onClick = onNavigateToSports
                    )
                }
            }
        }
    }
}
