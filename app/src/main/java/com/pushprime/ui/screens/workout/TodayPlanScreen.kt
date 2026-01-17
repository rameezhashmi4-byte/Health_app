package com.pushprime.ui.screens.workout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pushprime.ui.components.FeedCard
import com.pushprime.ui.theme.PushPrimeColors

/**
 * Today Plan Screen
 * Generates daily workout plans with friendly, family-safe messages
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayPlanScreen(
    onNavigateBack: () -> Unit,
    onStartWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val plans = remember {
        listOf(
            WorkoutPlan(
                title = "Leg Day + Football Conditioning",
                duration = "20 min",
                exercises = listOf("Squats", "Lunges", "Sprints", "Agility Drills"),
                description = "Perfect combo for leg strength and football fitness!"
            ),
            WorkoutPlan(
                title = "Full Body Blast",
                duration = "25 min",
                exercises = listOf("Push-ups", "Squats", "Plank", "Burpees"),
                description = "Get your whole body moving with this energizing workout!"
            ),
            WorkoutPlan(
                title = "Core & Cardio",
                duration = "15 min",
                exercises = listOf("Plank", "Mountain Climbers", "High Knees", "Jumping Jacks"),
                description = "Quick and effective core strengthening with cardio boost!"
            )
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Today's Plan",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PushPrimeColors.Surface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Suggested Workouts",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            
            items(plans.size) { index ->
                WorkoutPlanCard(
                    plan = plans[index],
                    onStart = onStartWorkout
                )
            }
        }
    }
}

data class WorkoutPlan(
    val title: String,
    val duration: String,
    val exercises: List<String>,
    val description: String
)

@Composable
fun WorkoutPlanCard(
    plan: WorkoutPlan,
    onStart: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = PushPrimeColors.Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plan.title,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = plan.duration,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PushPrimeColors.Primary
                    )
                }
                Icon(
                    Icons.Default.FitnessCenter,
                    contentDescription = "Workout",
                    tint = PushPrimeColors.Primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = plan.description,
                style = MaterialTheme.typography.bodyMedium,
                color = PushPrimeColors.OnSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Exercises:",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            plan.exercises.forEach { exercise ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = PushPrimeColors.Primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = exercise,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start Workout",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
