package com.pushprime.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
// Vico charts - simplified for MVP
// import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
// import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
// import com.patrykandpatrick.vico.compose.chart.Chart
// import com.patrykandpatrick.vico.compose.chart.line.lineChart
// import com.patrykandpatrick.vico.compose.chart.line.lineSpec
// import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
// import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
// import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.pushprime.ui.theme.PushPrimeColors
import java.text.SimpleDateFormat
import java.util.*

/**
 * Weekly Trend Chart Component
 * Shows 7-day trend for exercise data
 */
@Composable
fun WeeklyTrendChart(
    weeklyData: List<DailyDataPoint>,
    exerciseName: String? = null,
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
                text = exerciseName?.let { "Weekly Trend: $it" } ?: "Weekly Trend",
                style = MaterialTheme.typography.titleLarge,
                color = PushPrimeColors.OnSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (weeklyData.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = "No data for this week",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PushPrimeColors.OnSurfaceVariant
                    )
                }
            } else {
                // Simplified chart for MVP - show bar representation
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Day labels and bars
                    weeklyData.forEachIndexed { index, dataPoint ->
                        val dayLabel = SimpleDateFormat("EEE", Locale.getDefault()).format(dataPoint.date)
                        val maxValue = weeklyData.maxOfOrNull { it.total } ?: 1
                        val barWidth = if (maxValue > 0) {
                            (dataPoint.total.toFloat() / maxValue * 100).coerceAtMost(100f)
                        } else 0f
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = dayLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = PushPrimeColors.OnSurfaceVariant,
                                modifier = Modifier.width(40.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(24.dp)
                                    .background(
                                        color = PushPrimeColors.SurfaceVariant,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(barWidth / 100f)
                                        .background(
                                            color = PushPrimeColors.Primary,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                )
                            }
                            Text(
                                text = "${dataPoint.total}",
                                style = MaterialTheme.typography.bodySmall,
                                color = PushPrimeColors.OnSurface,
                                modifier = Modifier.width(40.dp)
                            )
                        }
                    }
                }
                
                // Summary stats
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        label = "Total",
                        value = weeklyData.sumOf { it.total }.toString()
                    )
                    StatItem(
                        label = "Avg/Day",
                        value = if (weeklyData.isNotEmpty()) {
                            (weeklyData.sumOf { it.total } / weeklyData.size).toString()
                        } else "0"
                    )
                    StatItem(
                        label = "Best Day",
                        value = weeklyData.maxOfOrNull { it.total }?.toString() ?: "0"
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = PushPrimeColors.Primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = PushPrimeColors.OnSurfaceVariant
        )
    }
}

/**
 * Data point for daily exercise totals
 */
data class DailyDataPoint(
    val date: Date,
    val total: Int
)
