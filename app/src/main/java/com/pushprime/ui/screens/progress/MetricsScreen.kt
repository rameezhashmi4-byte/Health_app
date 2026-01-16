package com.pushprime.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pushprime.data.ExerciseRepository
import com.pushprime.data.LocalStore
import com.pushprime.model.ExerciseLog
import com.pushprime.ui.components.DailyDataPoint
import com.pushprime.ui.components.MotivationMessage
import com.pushprime.ui.components.ProgressMetrics
import com.pushprime.ui.components.WeeklyTrendChart
import com.pushprime.ui.theme.PushPrimeColors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Metrics Screen
 * Shows weekly trends, progress metrics, and motivation
 */
@Composable
fun MetricsScreen(
    localStore: LocalStore,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val exerciseRepository = remember { ExerciseRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    
    var weeklyData by remember { mutableStateOf<List<DailyDataPoint>>(emptyList()) }
    var currentWeekTotal by remember { mutableStateOf(0) }
    var previousWeekTotal by remember { mutableStateOf(0) }
    var currentMonthTotal by remember { mutableStateOf(0) }
    var previousMonthTotal by remember { mutableStateOf(0) }
    var streak by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Load data
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                isLoading = true
                
                // Get weekly logs - use first() to get current snapshot
                val weeklyLogs = exerciseRepository.getWeeklyLogs()
                val logs = weeklyLogs.first()
            
            // Group by date
            val groupedByDate = logs.groupBy { it.date }
            val calendar = Calendar.getInstance()
            
            // Get last 7 days
            val dailyPoints = mutableListOf<DailyDataPoint>()
            for (i in 6 downTo 0) {
                calendar.time = Date()
                calendar.add(Calendar.DAY_OF_YEAR, -i)
                val date = calendar.time
                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
                val dayLogs = groupedByDate[dateStr] ?: emptyList()
                val total = dayLogs.sumOf { it.repsOrDuration }
                dailyPoints.add(DailyDataPoint(date, total))
            }
            weeklyData = dailyPoints
            
            // Calculate totals
            currentWeekTotal = logs.sumOf { it.repsOrDuration }
            
            // Get previous week
            calendar.time = Date()
            val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, -7)
            val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, -7)
            val prevStartDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            
            val previousWeekLogs = exerciseRepository.dao.getLogsByDateRange(prevStartDate, startDate)
            val prevLogs = previousWeekLogs.first()
            previousWeekTotal = prevLogs.sumOf { it.repsOrDuration }
            
            // Get month totals
            calendar.time = Date()
            val monthEnd = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            val monthStart = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            
            val currentMonthLogs = exerciseRepository.dao.getLogsByDateRange(monthStart, monthEnd)
            val monthLogs = currentMonthLogs.first()
            currentMonthTotal = monthLogs.sumOf { it.repsOrDuration }
            
            calendar.add(Calendar.MONTH, -1)
            val prevMonthEnd = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            val prevMonthStart = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            
            val previousMonthLogs = exerciseRepository.dao.getLogsByDateRange(prevMonthStart, prevMonthEnd)
            val prevMonthLogs = previousMonthLogs.first()
            previousMonthTotal = prevMonthLogs.sumOf { it.repsOrDuration }
            
                // Get streak from recent exercise logs
                streak = calculateLogStreak(logs)
                
                isLoading = false
            } catch (e: Exception) {
                // Handle errors gracefully - show empty state
                isLoading = false
                weeklyData = emptyList()
                currentWeekTotal = 0
                previousWeekTotal = 0
                currentMonthTotal = 0
                previousMonthTotal = 0
                streak = 0
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PushPrimeColors.Background)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Metrics & Progress",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = PushPrimeColors.OnSurface
            )
        }
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PushPrimeColors.Primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Motivation Message
                item {
                    MotivationMessage(
                        currentWeekTotal = currentWeekTotal,
                        previousWeekTotal = previousWeekTotal,
                        streak = streak
                    )
                }
                
                // Weekly Trend Chart
                item {
                    WeeklyTrendChart(weeklyData = weeklyData)
                }
                
                // Progress Metrics
                item {
                    ProgressMetrics(
                        currentWeekTotal = currentWeekTotal,
                        previousWeekTotal = previousWeekTotal,
                        currentMonthTotal = currentMonthTotal,
                        previousMonthTotal = previousMonthTotal
                    )
                }
            }
        }
    }
}

private fun calculateLogStreak(logs: List<ExerciseLog>): Int {
    if (logs.isEmpty()) return 0
    val availableDates = logs.map { it.date }.toSet()
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()
    var streak = 0
    while (true) {
        val dateKey = dateFormatter.format(calendar.time)
        if (availableDates.contains(dateKey)) {
            streak++
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        } else {
            break
        }
    }
    return streak
}
