package com.pushprime.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pushprime.data.SessionDao
import com.pushprime.model.ActivityType
import com.pushprime.ui.theme.PushPrimeColors
import java.text.SimpleDateFormat
import java.util.*

/**
 * Calendar Screen
 * Month grid view with session indicators
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    sessionDao: SessionDao,
    onDayClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val calendar = remember { Calendar.getInstance() }
    val currentMonth = remember { calendar.get(Calendar.MONTH) }
    val currentYear = remember { calendar.get(Calendar.YEAR) }
    
    var selectedMonth by remember { mutableStateOf(currentMonth) }
    var selectedYear by remember { mutableStateOf(currentYear) }
    
    val monthStart = remember(selectedMonth, selectedYear) {
        calendar.set(selectedYear, selectedMonth, 1)
        calendar.time
    }
    
    val daysInMonth = remember(selectedMonth, selectedYear) {
        calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
    
    val firstDayOfWeek = remember(selectedMonth, selectedYear) {
        calendar.set(selectedYear, selectedMonth, 1)
        calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday
    }
    
    // Load session counts for the month
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val startDate = dateFormat.format(monthStart)
    val endDate = dateFormat.format(Calendar.getInstance().apply {
        set(selectedYear, selectedMonth, daysInMonth)
    }.time)
    
    val sessionCounts by sessionDao.getSessionsByDateRange(startDate, endDate)
        .collectAsState(initial = emptyList())
    
    val sessionCountMap = remember(sessionCounts) {
        sessionCounts.groupBy { it.date }.mapValues { it.value.size }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                            .format(Calendar.getInstance().apply {
                                set(selectedYear, selectedMonth, 1)
                            }.time),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (selectedMonth > 0) {
                            selectedMonth--
                        } else {
                            selectedMonth = 11
                            selectedYear--
                        }
                    }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous")
                    }
                    IconButton(onClick = {
                        if (selectedMonth < 11) {
                            selectedMonth++
                        } else {
                            selectedMonth = 0
                            selectedYear++
                        }
                    }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PushPrimeColors.Surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Week day headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PushPrimeColors.OnSurfaceVariant,
                        modifier = Modifier.weight(1f).padding(4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Calendar grid
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                var dayNumber = 1
                var currentWeek = 0
                
                while (dayNumber <= daysInMonth) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (dayOfWeek in 0..6) {
                            if (currentWeek == 0 && dayOfWeek < firstDayOfWeek) {
                                // Empty cell before first day
                                Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                            } else if (dayNumber <= daysInMonth) {
                                val dateStr = String.format(
                                    Locale.getDefault(),
                                    "%04d-%02d-%02d",
                                    selectedYear,
                                    selectedMonth + 1,
                                    dayNumber
                                )
                                val hasSessions = sessionCountMap.containsKey(dateStr)
                                
                                CalendarDayCell(
                                    day = dayNumber,
                                    hasSessions = hasSessions,
                                    isToday = dateStr == SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                        .format(Date()),
                                    onClick = { onDayClick(dateStr) },
                                    modifier = Modifier.weight(1f)
                                )
                                dayNumber++
                            } else {
                                Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                    currentWeek++
                }
            }
        }
    }
}

@Composable
fun CalendarDayCell(
    day: Int,
    hasSessions: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clickable(onClick = onClick)
            .background(
                color = if (isToday) PushPrimeColors.Primary.copy(alpha = 0.2f) else PushPrimeColors.Surface,
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = if (isToday) PushPrimeColors.Primary else PushPrimeColors.OnSurface
            )
            if (hasSessions) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            color = PushPrimeColors.Primary,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}
