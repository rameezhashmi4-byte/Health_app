package com.pushprime.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pushprime.model.LeaderboardEntry
import com.pushprime.ui.theme.PushPrimeColors

/**
 * Leaderboard Card Component
 * Displays leaderboard entry with rank, flag, and stats
 * Top 3 highlighted with special styling
 */
@Composable
fun LeaderboardCard(
    entry: LeaderboardEntry,
    modifier: Modifier = Modifier
) {
    val isTopThree = entry.rank <= 3
    val backgroundColor = when (entry.rank) {
        1 -> PushPrimeColors.Warning.copy(alpha = 0.15f) // Gold
        2 -> PushPrimeColors.Outline.copy(alpha = 0.3f) // Silver
        3 -> PushPrimeColors.Primary.copy(alpha = 0.1f) // Bronze
        else -> PushPrimeColors.Surface
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isTopThree) 4.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank and Flag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isTopThree) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = when (entry.rank) {
                            1 -> PushPrimeColors.Warning
                            2 -> PushPrimeColors.Outline
                            else -> PushPrimeColors.Primary
                        },
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Text(
                        text = "#${entry.rank}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PushPrimeColors.OnSurfaceVariant
                    )
                }
                
                Text(
                    text = entry.getCountryFlag(),
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Column {
                    Text(
                        text = entry.username,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PushPrimeColors.OnSurface
                    )
                    Text(
                        text = entry.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = PushPrimeColors.OnSurfaceVariant
                    )
                }
            }
            
            // Stats
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${entry.pushups}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = PushPrimeColors.Primary
                )
                Text(
                    text = "push-ups",
                    style = MaterialTheme.typography.bodySmall,
                    color = PushPrimeColors.OnSurfaceVariant
                )
                Text(
                    text = formatTime(entry.workoutTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = PushPrimeColors.OnSurfaceVariant
                )
            }
        }
    }
}

fun formatTime(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format("%d:%02d", minutes, secs)
    }
}
