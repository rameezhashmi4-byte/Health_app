package com.pushprime.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pushprime.ui.theme.PushPrimeColors
import kotlin.math.roundToInt

/**
 * Progress Metrics Component
 * Shows "where you were" vs "where you are" comparison
 */
@Composable
fun ProgressMetrics(
    currentWeekTotal: Int,
    previousWeekTotal: Int,
    currentMonthTotal: Int,
    previousMonthTotal: Int,
    modifier: Modifier = Modifier
) {
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
                text = "Your Progress",
                style = MaterialTheme.typography.titleLarge,
                color = PushPrimeColors.OnSurface
            )
            Spacer(modifier = Modifier.height(20.dp))
            
            // Week Comparison
            ComparisonCard(
                title = "This Week",
                current = currentWeekTotal,
                previous = previousWeekTotal,
                period = "vs Last Week"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Month Comparison
            ComparisonCard(
                title = "This Month",
                current = currentMonthTotal,
                previous = previousMonthTotal,
                period = "vs Last Month"
            )
        }
    }
}

@Composable
private fun ComparisonCard(
    title: String,
    current: Int,
    previous: Int,
    period: String
) {
    val change = current - previous
    val percentChange = if (previous > 0) {
        ((change.toFloat() / previous) * 100).roundToInt()
    } else if (current > 0) {
        100 // 100% increase from 0
    } else {
        0
    }
    
    val isImprovement = change >= 0
    val changeColor = if (isImprovement) {
        PushPrimeColors.Success
    } else {
        PushPrimeColors.Error
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = PushPrimeColors.Background
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = PushPrimeColors.OnSurface
                    )
                    Text(
                        text = period,
                        style = MaterialTheme.typography.bodySmall,
                        color = PushPrimeColors.OnSurfaceVariant
                    )
                }
                Text(
                    text = "$current",
                    style = MaterialTheme.typography.headlineMedium,
                    color = PushPrimeColors.Primary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Change indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (isImprovement) "↑" else "↓",
                    style = MaterialTheme.typography.titleLarge,
                    color = changeColor
                )
                Text(
                    text = "${if (change >= 0) "+" else ""}$change (${if (percentChange >= 0) "+" else ""}$percentChange%)",
                    style = MaterialTheme.typography.bodyLarge,
                    color = changeColor
                )
                Text(
                    text = if (isImprovement) "improvement!" else "to work on",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PushPrimeColors.OnSurfaceVariant
                )
            }
        }
    }
}
