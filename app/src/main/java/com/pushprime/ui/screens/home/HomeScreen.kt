package com.pushprime.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pushprime.ui.components.RamboostCard
import com.pushprime.ui.components.RamboostPrimaryButton
import com.pushprime.ui.components.RamboostSecondaryButton
import com.pushprime.ui.components.RamboostStatTile
import com.pushprime.ui.components.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToProfile: () -> Unit,
    onNavigateToWorkout: (String) -> Unit,
    onNavigateToSessionMode: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToQuickSession: () -> Unit,
    onNavigateToProgressPhotos: () -> Unit,
    onNavigateToSportsMode: () -> Unit,
    onNavigateToWorkoutGenerator: () -> Unit,
    onNavigateToSavedPlan: (Long) -> Unit,
    onNavigateToStreakDetails: () -> Unit,
    onNavigateToMusicMode: () -> Unit,
    onNavigateToNutrition: () -> Unit = {},
    onNavigateToPullups: () -> Unit = {},
    onNavigateToAiCoach: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            // 1) Top Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hey, ${uiState.user?.username ?: "RAMBOOSTER"}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Let's get a win today.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onNavigateToProfile,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // 2) Hero Card
            item {
                RamboostCard(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    onClick = { onNavigateToSessionMode() }
                ) {
                    Text(
                        text = "TODAY'S FOCUS",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Text(
                        text = uiState.user?.fitnessLevel?.name?.replace("_", " ") ?: "BUILD MUSCLE",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = "🔥 ${uiState.streak}-day streak",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold
                    )
                    if (uiState.lastWorkoutLabel != null) {
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            text = "Last workout: ${uiState.lastWorkoutLabel}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(modifier = Modifier.height(Spacing.lg))

                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        RamboostPrimaryButton(
                            text = "Start Session",
                            onClick = onNavigateToSessionMode,
                            fullWidth = false,
                            modifier = Modifier.weight(1f),
                            containerColor = MaterialTheme.colorScheme.onPrimary,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                        RamboostSecondaryButton(
                            text = "Log Manual",
                            onClick = onNavigateToProgress,
                            fullWidth = false,
                            modifier = Modifier.weight(1f),
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            borderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // 2.3) Streak Card
            item {
                StreakCard(
                    currentStreak = uiState.streak,
                    bestStreak = uiState.bestStreakDays,
                    freezeTokensRemaining = uiState.freezeTokensRemaining,
                    restDaysLeft = uiState.restDaysLeftThisWeek,
                    isFrozenToday = uiState.isStreakProtectedToday,
                    hasWorkoutToday = uiState.hasWorkoutToday,
                    isRestDayToday = uiState.isRestDayToday,
                    onMarkRestDay = { viewModel.markRestDay() },
                    onStartQuickSession = onNavigateToQuickSession,
                    onOpenDetails = onNavigateToStreakDetails
                )
            }

            // 2.5) History Shortcut
            item {
                RamboostCard(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = { onNavigateToHistory() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "History",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Review every session",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // 2.6) Progress Photos Shortcut
            item {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToProgressPhotos() }
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Progress Photos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                            Text(
                                "Track your transformation",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color.Black)
                    }
                }
            }

            // 2.75) Sports Mode Shortcut
            item {
                RamboostCard(
                    containerColor = MaterialTheme.colorScheme.surface,
                    onClick = { onNavigateToSportsMode() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sports Mode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Pick a sport and go beast mode",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text("🏉")
                    }
                }
            }

            // 3) Quick Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatTile(
                        label = "Steps",
                        value = if (uiState.isStepsEnabled) uiState.todaySteps.toString() else "Off",
                        icon = Icons.Default.DirectionsWalk,
                        enabled = uiState.isStepsEnabled,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        label = "Sessions",
                        value = uiState.weeklySessions.toString(),
                        icon = Icons.Default.Event,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        label = "Burned",
                        value = "${uiState.caloriesBurned}",
                        icon = Icons.Default.Whatshot,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 4) Weekly Progress
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "WEEKLY PROGRESS",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    WeeklyChart(progress = uiState.weeklyProgress)
                }
            }

            // 5) Workout Plan Section
            item {
                Surface(
                    color = Color(0xFFF6F6F6),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "YOUR PLAN",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        if (uiState.savedPlans.isNotEmpty()) {
                            val latestPlan = uiState.savedPlans.first()
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToSavedPlan(latestPlan.id) }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = latestPlan.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = Color.Black
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "${latestPlan.totalDurationMinutes} min • ${latestPlan.goal.displayName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { onNavigateToSavedPlan(latestPlan.id) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                                    ) {
                                        Text("View Plan", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            PlanButton("Push", Modifier.weight(1f)) { onNavigateToWorkout("PUSH") }
                            PlanButton("Pull", Modifier.weight(1f)) { onNavigateToWorkout("PULL") }
                            PlanButton("Legs", Modifier.weight(1f)) { onNavigateToWorkout("LEGS") }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { onNavigateToWorkout("SPORTS") },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Sports Mode ⚽", fontWeight = FontWeight.Black)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onNavigateToWorkoutGenerator,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Workout Generator", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 6) Session Mode Screen entry
            item {
                Surface(
                    color = Color(0xFF276EF1), // Uber Blue
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToSessionMode() }
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Session Mode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                "Focus + Music + Voice",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
                    }
                }
            }

            item {
                Surface(
                    color = Color.Black,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToMusicMode() }
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Music Mode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                "Warm-up • Main • Finisher energy",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun StreakCard(
    currentStreak: Int,
    bestStreak: Int,
    freezeTokensRemaining: Int,
    restDaysLeft: Int,
    isFrozenToday: Boolean,
    hasWorkoutToday: Boolean,
    isRestDayToday: Boolean,
    onMarkRestDay: () -> Unit,
    onStartQuickSession: () -> Unit,
    onOpenDetails: () -> Unit
) {
    val canMarkRestDay = !hasWorkoutToday && !isRestDayToday && restDaysLeft > 0
    Surface(
        color = Color(0xFFF6F6F6),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenDetails() }
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Streak",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                    Text(
                        text = "Stay consistent, stay strong",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                if (isFrozenToday) {
                    Surface(
                        color = Color(0xFFDCEBFF),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Streak Protected ❄️",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D4ED8)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "🔥 Current streak: $currentStreak days",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "🏆 Best: $bestStreak days",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "❄️ Freeze tokens: $freezeTokensRemaining left",
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray
            )
            Text(
                text = "Rest days left this week: $restDaysLeft",
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (!hasWorkoutToday) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onMarkRestDay,
                        enabled = canMarkRestDay,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canMarkRestDay) Color.Black else Color.Gray,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Text(
                            text = if (isRestDayToday) "Rest Day Marked" else "Mark Rest Day",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    OutlinedButton(
                        onClick = onStartQuickSession,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Text("Start 10-min Quick Session", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            } else {
                Text(
                    text = "Workout logged today ✅",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF22C55E),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onStartQuickSession,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text("Start 10-min Quick Session", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun StatTile(label: String, value: String, icon: ImageVector, enabled: Boolean = true, modifier: Modifier = Modifier) {
    Surface(
        color = Color(0xFFF6F6F6),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (enabled) Color.Black else Color.Gray
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, fontWeight = FontWeight.Black, fontSize = 18.sp, color = if (enabled) Color.Black else Color.Gray)
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            if (!enabled) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Enable", color = Color(0xFF276EF1), fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.clickable { /* Enable */ })
            }
        }
    }
}

@Composable
fun PlanButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clickable { onClick() },
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun WeeklyChart(progress: List<Int>) {
    val labels = listOf("M", "T", "W", "T", "F", "S", "S")
    val maxVal = progress.maxOrNull()?.coerceAtLeast(1) ?: 1
    
    Row(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        progress.forEachIndexed { index, value ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .fillMaxHeight((value.toFloat() / maxVal).coerceIn(0.1f, 1f))
                        .background(if (value > 0) Color.Black else Color.LightGray, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(labels[index], style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}

