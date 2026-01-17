package com.pushprime.ui.screens.progress

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.pushprime.data.SessionDao
import com.pushprime.data.WeeklyAggregationResult
import com.pushprime.data.MonthlyAggregationResult
import com.pushprime.model.ActivityType
import com.pushprime.model.SessionEntity
import com.pushprime.ui.components.AppCard
import com.pushprime.ui.components.AppChoiceChip
import com.pushprime.ui.components.PremiumFadeSlideIn
import com.pushprime.ui.screens.common.ErrorScreen
import com.pushprime.ui.theme.AppSpacing
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.flow.first

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
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        PremiumFadeSlideIn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(AppSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)
            ) {
                // Period selector
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        AnalyticsPeriod.values().forEach { period ->
                            AppChoiceChip(
                                label = period.displayName,
                                selected = selectedPeriod == period,
                                onSelectedChange = { if (it) selectedPeriod = period },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        ActivityFilter.values().forEach { filter ->
                            AppChoiceChip(
                                label = filter.displayName,
                                selected = selectedFilter == filter,
                                onSelectedChange = { if (it) selectedFilter = filter },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                
                // Summary cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
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
                    AppCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PieChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(AppSpacing.sm))
                                Text(
                                    text = "Activity Breakdown",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                            Spacer(modifier = Modifier.height(AppSpacing.md))
                            monthlyData.forEach { data: MonthlyAggregationResult ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = AppSpacing.xs),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = data.activityType,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "${data.count} sessions",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Weekly trend
                item {
                    AppCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(AppSpacing.sm))
                                Text(
                                    text = "Weekly Trend",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                            Spacer(modifier = Modifier.height(AppSpacing.md))
                            if (weeklyData.isEmpty()) {
                                Text(
                                    text = "No data for this period",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                weeklyData.forEach { dayData: WeeklyAggregationResult ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = AppSpacing.xs),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = dayData.date,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = "${dayData.count} sessions",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
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
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(AppSpacing.sm))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
