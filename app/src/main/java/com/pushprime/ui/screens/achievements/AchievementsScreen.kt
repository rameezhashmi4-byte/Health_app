package com.pushprime.ui.screens.achievements

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pushprime.model.Achievement
import com.pushprime.model.AchievementType
import com.pushprime.ui.theme.PushPrimeColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AchievementsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToShareProgress: () -> Unit = {},
    viewModel: AchievementsViewModel = hiltViewModel()
) {
    val achievements by viewModel.achievements.collectAsState()
    val summary by viewModel.summary.collectAsState()
    var selected by remember { mutableStateOf<Achievement?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Achievements",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = "Earn badges by showing up.",
                            style = MaterialTheme.typography.bodySmall,
                            color = PushPrimeColors.OnSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToShareProgress) {
                        Icon(Icons.Default.Share, contentDescription = "Share Progress")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PushPrimeColors.Surface
                )
            )
        },
        containerColor = PushPrimeColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AchievementSummaryCard(
                totalUnlocked = summary.totalUnlocked,
                totalBadges = summary.totalBadges,
                streak = summary.currentStreak,
                sessionsCompleted = summary.sessionsCompleted
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(achievements, key = { it.id }) { achievement ->
                    AchievementBadgeCard(
                        achievement = achievement,
                        onClick = { selected = achievement }
                    )
                }
            }
        }
    }

    if (selected != null) {
        ModalBottomSheet(
            onDismissRequest = { selected = null }
        ) {
            AchievementDetailSheet(achievement = selected!!)
        }
    }
}

@Composable
private fun AchievementSummaryCard(
    totalUnlocked: Int,
    totalBadges: Int,
    streak: Int,
    sessionsCompleted: Int
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PushPrimeColors.Surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Progress Summary",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryStat(label = "Badges", value = "$totalUnlocked / $totalBadges")
                SummaryStat(label = "Streak", value = "$streak days")
                SummaryStat(label = "Sessions", value = "$sessionsCompleted")
            }
        }
    }
}

@Composable
private fun SummaryStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = PushPrimeColors.OnSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AchievementBadgeCard(
    achievement: Achievement,
    onClick: () -> Unit
) {
    val locked = !achievement.unlocked
    val cardColor = if (locked) PushPrimeColors.SurfaceVariant else PushPrimeColors.Surface
    val contentColor = if (locked) PushPrimeColors.OnSurfaceVariant else PushPrimeColors.OnSurface

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = achievement.icon,
                    style = MaterialTheme.typography.headlineSmall
                )
                if (locked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = PushPrimeColors.OnSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.titleLarge,
                color = contentColor
            )
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor
            )
            if (locked) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = progressLabel(achievement),
                    style = MaterialTheme.typography.labelSmall,
                    color = PushPrimeColors.OnSurfaceVariant
                )
                LinearProgressIndicator(
                    progress = progressFraction(achievement),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = PushPrimeColors.Primary,
                    trackColor = PushPrimeColors.Outline
                )
            }
        }
    }
}

@Composable
private fun AchievementDetailSheet(achievement: Achievement) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        if (achievement.unlocked) PushPrimeColors.Primary.copy(alpha = 0.15f)
                        else PushPrimeColors.SurfaceVariant,
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = achievement.icon, style = MaterialTheme.typography.headlineSmall)
            }
            Column {
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PushPrimeColors.OnSurfaceVariant
                )
            }
        }
        Text(
            text = "Requirement",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = progressLabel(achievement),
            style = MaterialTheme.typography.bodyMedium,
            color = PushPrimeColors.OnSurfaceVariant
        )
        LinearProgressIndicator(
            progress = progressFraction(achievement),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = PushPrimeColors.Primary,
            trackColor = PushPrimeColors.Outline
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

private fun progressFraction(achievement: Achievement): Float {
    if (achievement.threshold <= 0) return 0f
    return (achievement.progress.toFloat() / achievement.threshold).coerceIn(0f, 1f)
}

private fun progressLabel(achievement: Achievement): String {
    return when (achievement.type) {
        AchievementType.STREAK -> "${achievement.progress} / ${achievement.threshold} days"
        AchievementType.SESSIONS -> "${achievement.progress} / ${achievement.threshold} sessions"
        AchievementType.SPORTS -> {
            if (achievement.id == "sports_beast_10") {
                "${achievement.progress} / ${achievement.threshold} high-effort"
            } else {
                "${achievement.progress} / ${achievement.threshold} sessions"
            }
        }
        AchievementType.STEPS -> {
            if (achievement.id == "steps_streak_7") {
                "${achievement.progress} / ${achievement.threshold} days"
            } else {
                "${achievement.progress} / ${achievement.threshold} steps"
            }
        }
    }
}
