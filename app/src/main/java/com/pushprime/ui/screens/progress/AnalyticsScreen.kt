package com.pushprime.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pushprime.data.SessionDao
import com.pushprime.data.WeeklyAggregationResult
import com.pushprime.data.MonthlyAggregationResult
import com.pushprime.model.ActivityType
import com.pushprime.model.SessionEntity
import com.pushprime.ui.components.FeedCard
import com.pushprime.ui.theme.PushPrimeColors
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.flow.first

/**
 * Analytics Screen
 * Weekly/monthly/yearly aggregations and charts
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    sessionDao: SessionDao?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (sessionDao == null) {
        ErrorScreen(message = "Database not available")
        return
    }
    
    var selectedPeriod by remember { mutableStateOf<AnalyticsPeriod>(AnalyticsPeriod.WEEKLY) }
    var selectedFilter by remember { mutableStateOf<ActivityFilter>(ActivityFilter.ALL) }
    
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()
    
    val (startDate, endDate) = remember(selectedPeriod) {
        when (selectedPeriod) {
            AnalyticsPeriod.WEEKLY -> {
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                val weekStart = dateFormat.format(calendar.time)
                calendar.add(Calendar.DAY_OF_WEEK, 6)
                val weekEnd = dateFormat.format(calendar.time)
                weekStart to weekEnd
            }
            AnalyticsPeriod.MONTHLY -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val monthStart = dateFormat.format(calendar.time)
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                val monthEnd = dateFormat.format(calendar.time)
                monthStart to monthEnd
            }
            AnalyticsPeriod.YEARLY -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                val yearStart = dateFormat.format(calendar.time)
                calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR))
                val yearEnd = dateFormat.format(calendar.time)
                yearStart to yearEnd
            }
        }
    }
    
    var weeklyData by remember { mutableStateOf<List<WeeklyAggregationResult>>(emptyList()) }
    var monthlyData by remember { mutableStateOf<List<MonthlyAggregationResult>>(emptyList()) }
    
    LaunchedEffect(startDate, endDate, selectedFilter) {
        val sessions = sessionDao.getSessionsByDateRange(startDate, endDate).first()
        val filteredSessions = when (selectedFilter) {
            ActivityFilter.ALL -> sessions
            ActivityFilter.GYM -> sessions.filter { it.activityType == ActivityType.GYM.name }
            ActivityFilter.SPORT -> sessions.filter { it.activityType == ActivityType.SPORT.name }
        }
        weeklyData = buildDailyAggregation(startDate, endDate, filteredSessions)
        monthlyData = buildMonthlyAggregation(filteredSessions)
    }
    
    val totalSessions = remember(weeklyData) {
        weeklyData.sumOf { it.count }
    }
    
    val totalMinutes = remember(weeklyData) {
        (weeklyData.sumOf { (it.totalSeconds ?: 0) } / 60).toInt()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Analytics",
                        fontWeight = FontWeight.Bold
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
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Period selector
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AnalyticsPeriod.values().forEach { period ->
                        FilterChip(
                            selected = selectedPeriod == period,
                            onClick = { selectedPeriod = period },
                            label = { Text(period.displayName) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActivityFilter.values().forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter.displayName) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // Summary cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        title = "Sessions",
                        value = totalSessions.toString(),
                        icon = Icons.Default.FitnessCenter,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Minutes",
                        value = totalMinutes.toString(),
                        icon = Icons.Default.Timer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Activity breakdown
            item {
                FeedCard(
                    title = "Activity Breakdown",
                    icon = Icons.Default.PieChart
                ) {
                    monthlyData.forEach { data: MonthlyAggregationResult ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = data.activityType,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${data.count} sessions",
                                style = MaterialTheme.typography.bodyMedium,
                                color = PushPrimeColors.OnSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Weekly chart (simplified for MVP)
            item {
                FeedCard(
                    title = "Weekly Trend",
                    icon = Icons.Default.TrendingUp
                ) {
                    if (weeklyData.isEmpty()) {
                        Text(
                            text = "No data for this period",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PushPrimeColors.OnSurfaceVariant
                        )
                    } else {
                        weeklyData.forEach { dayData: WeeklyAggregationResult ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = dayData.date,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "${dayData.count} sessions",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PushPrimeColors.OnSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = PushPrimeColors.Surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PushPrimeColors.Primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = PushPrimeColors.OnSurfaceVariant
            )
        }
    }
}

enum class AnalyticsPeriod(val displayName: String) {
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly")
}

enum class ActivityFilter(val displayName: String) {
    ALL("All"),
    GYM("Gym"),
    SPORT("Sports")
}

private fun buildDailyAggregation(
    startDate: String,
    endDate: String,
    sessions: List<SessionEntity>
): List<WeeklyAggregationResult> {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val start = dateFormat.parse(startDate) ?: return emptyList()
    val end = dateFormat.parse(endDate) ?: return emptyList()
    val calendar = Calendar.getInstance().apply { time = start }
    val results = mutableListOf<WeeklyAggregationResult>()
    while (!calendar.time.after(end)) {
        val dateKey = dateFormat.format(calendar.time)
        val daySessions = sessions.filter { it.date == dateKey }
        val totalSeconds = daySessions.sumOf { it.totalSeconds ?: it.getDurationSeconds() }
        results.add(
            WeeklyAggregationResult(
                date = dateKey,
                count = daySessions.size,
                totalSeconds = totalSeconds
            )
        )
        calendar.add(Calendar.DAY_OF_YEAR, 1)
    }
    return results
}

private fun buildMonthlyAggregation(
    sessions: List<SessionEntity>
): List<MonthlyAggregationResult> {
    return sessions.groupBy { it.activityType }
        .map { (activityType, group) ->
            val totalSeconds = group.sumOf { it.totalSeconds ?: it.getDurationSeconds() }
            MonthlyAggregationResult(
                activityType = activityType,
                count = group.size,
                totalSeconds = totalSeconds
            )
        }
}
