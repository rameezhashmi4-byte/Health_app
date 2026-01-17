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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pushprime.ui.components.AppCard
import com.pushprime.ui.components.AppPrimaryButton
import com.pushprime.ui.components.AppSecondaryButton
import com.pushprime.ui.components.AppTextButton
import com.pushprime.ui.components.PremiumFadeSlideIn
import com.pushprime.ui.theme.AppSpacing

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
        PremiumFadeSlideIn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(AppSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)
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
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Let's get a win today.",
                                style = MaterialTheme.typography.bodyLarge,
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
                    AppCard(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        onClick = { onNavigateToSessionMode() }
                    ) {
                        Column {
                            Text(
                                text = "TODAY'S FOCUS",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(AppSpacing.sm))
                            Text(
                                text = uiState.user?.fitnessLevel?.name?.replace("_", " ") ?: "BUILD MUSCLE",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.height(AppSpacing.xs))
                            Text(
                                text = "🔥 ${uiState.streak}-day streak",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            if (uiState.lastWorkoutLabel != null) {
                                Spacer(modifier = Modifier.height(AppSpacing.xs))
                                Text(
                                    text = "Last: ${uiState.lastWorkoutLabel}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                )
                            }
                            Spacer(modifier = Modifier.height(AppSpacing.lg))

                            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                                AppPrimaryButton(
                                    text = "Start Session",
                                    onClick = onNavigateToSessionMode,
                                    fullWidth = false,
                                    modifier = Modifier.weight(1f)
                                )
                                AppSecondaryButton(
                                    text = "Log Manual",
                                    onClick = onNavigateToProgress,
                                    fullWidth = false,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // 3) Streak Card
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

                // 4) Quick Actions Grid (Replaces the vertical list for premium feel)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                            ActionCard(
                                title = "History",
                                icon = Icons.Default.History,
                                onClick = onNavigateToHistory,
                                modifier = Modifier.weight(1f)
                            )
                            ActionCard(
                                title = "Photos",
                                icon = Icons.Default.PhotoCamera,
                                onClick = onNavigateToProgressPhotos,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                            ActionCard(
                                title = "Sports",
                                icon = Icons.Default.SportsFootball,
                                onClick = onNavigateToSportsMode,
                                modifier = Modifier.weight(1f)
                            )
                            ActionCard(
                                title = "Music",
                                icon = Icons.Default.MusicNote,
                                onClick = onNavigateToMusicMode,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 5) Quick Stats Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
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

                // 6) Weekly Progress
                item {
                    AppCard(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Column {
                            Text(
                                "WEEKLY PROGRESS",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(AppSpacing.lg))
                            WeeklyChart(progress = uiState.weeklyProgress)
                        }
                    }
                }

                // 7) Workout Plan Section
                item {
                    AppCard(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentPadding = PaddingValues(AppSpacing.lg)
                    ) {
                        Column {
                            Text(
                                "YOUR PLAN",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.height(AppSpacing.lg))
                            if (uiState.savedPlans.isNotEmpty()) {
                                val latestPlan = uiState.savedPlans.first()
                                AppCard(
                                    onClick = { onNavigateToSavedPlan(latestPlan.id) },
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentPadding = PaddingValues(AppSpacing.lg)
                                ) {
                                    Column {
                                        Text(
                                            text = latestPlan.title,
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                        Spacer(modifier = Modifier.height(AppSpacing.xs))
                                        Text(
                                            text = "${latestPlan.totalDurationMinutes} min • ${latestPlan.goal.displayName}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(AppSpacing.md))
                                        AppPrimaryButton(
                                            text = "View Plan",
                                            onClick = { onNavigateToSavedPlan(latestPlan.id) }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(AppSpacing.md))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                                AppSecondaryButton(text = "Push", modifier = Modifier.weight(1f)) { onNavigateToWorkout("PUSH") }
                                AppSecondaryButton(text = "Pull", modifier = Modifier.weight(1f)) { onNavigateToWorkout("PULL") }
                                AppSecondaryButton(text = "Legs", modifier = Modifier.weight(1f)) { onNavigateToWorkout("LEGS") }
                            }
                            Spacer(modifier = Modifier.height(AppSpacing.md))
                            AppPrimaryButton(
                                text = "Sports Mode ⚽",
                                onClick = { onNavigateToWorkout("SPORTS") }
                            )
                            Spacer(modifier = Modifier.height(AppSpacing.md))
                            AppSecondaryButton(
                                text = "Workout Generator",
                                onClick = onNavigateToWorkoutGenerator
                            )
                        }
                    }
                }

                // Footer padding
                item {
                    Spacer(modifier = Modifier.height(AppSpacing.xxl))
                }
            }
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(
        onClick = onClick,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentPadding = PaddingValues(AppSpacing.lg)
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
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
    AppCard(
        onClick = onOpenDetails,
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        contentPadding = PaddingValues(AppSpacing.lg)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Streak",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Stay consistent",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (isFrozenToday) {
                    AppCard(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentPadding = PaddingValues(horizontal = AppSpacing.sm, vertical = AppSpacing.xs)
                    ) {
                        Text(
                            text = "Streak Protected ❄️",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(AppSpacing.lg))
            Text(
                text = "🔥 $currentStreak days",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "🏆 Best: $bestStreak days",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(AppSpacing.md))
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
                Text(
                    text = "❄️ $freezeTokensRemaining left",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Rest days: $restDaysLeft left",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(AppSpacing.lg))
            if (!hasWorkoutToday) {
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                    AppPrimaryButton(
                        text = if (isRestDayToday) "Rest Day" else "Rest Day",
                        onClick = onMarkRestDay,
                        enabled = canMarkRestDay,
                        modifier = Modifier.weight(1f)
                    )
                    AppSecondaryButton(
                        text = "Quick Session",
                        onClick = onStartQuickSession,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(AppSpacing.xs))
                    Text(
                        text = "Workout logged today",
                        style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF22C55E)
                    )
                }
                Spacer(modifier = Modifier.height(AppSpacing.md))
                AppPrimaryButton(
                    text = "Start 10-min Quick Session",
                    onClick = onStartQuickSession
                )
            }
        }
    }
}

@Composable
fun StatTile(label: String, value: String, icon: ImageVector, enabled: Boolean = true, modifier: Modifier = Modifier) {
    AppCard(
        modifier = modifier,
        contentPadding = PaddingValues(AppSpacing.lg)
    ) {
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(AppSpacing.md))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
            val heightPercent = (value.toFloat() / maxVal).coerceIn(0.1f, 1f)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .fillMaxHeight(heightPercent)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(if (value > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                )
                Spacer(modifier = Modifier.height(AppSpacing.sm))
                Text(
                    labels.getOrNull(index) ?: "", 
                    style = MaterialTheme.typography.labelSmall, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
