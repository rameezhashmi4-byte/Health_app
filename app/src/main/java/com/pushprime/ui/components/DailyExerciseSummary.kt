package com.pushprime.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pushprime.ui.theme.PushPrimeColors

/**
 * Daily Exercise Summary Component
 * Shows quick stats for each exercise today
 */
@Composable
fun DailyExerciseSummary(
    summary: Map<String, Int>, // exercise name -> total
    modifier: Modifier = Modifier
) {
    if (summary.isEmpty()) {
        return
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = PushPrimeColors.Surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Today's Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = PushPrimeColors.OnSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(summary.entries.toList()) { (exerciseName, total) ->
                    SummaryChip(
                        exerciseName = exerciseName,
                        total = total
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryChip(
    exerciseName: String,
    total: Int
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = PushPrimeColors.Primary.copy(alpha = 0.1f),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "🔥",
                style = MaterialTheme.typography.bodyLarge
            )
            Column {
                Text(
                    text = "$total",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PushPrimeColors.Primary
                )
                Text(
                    text = exerciseName,
                    style = MaterialTheme.typography.bodySmall,
                    color = PushPrimeColors.OnSurfaceVariant
                )
            }
        }
    }
}
