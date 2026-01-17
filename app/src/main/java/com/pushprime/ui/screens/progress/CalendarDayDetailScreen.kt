package com.pushprime.ui.screens.progress

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.pushprime.model.SessionEntity
import com.pushprime.ui.theme.PushPrimeColors
import androidx.compose.runtime.collectAsState
import java.text.SimpleDateFormat
import java.util.*

/**
 * Calendar Day Detail Screen
 * Shows all sessions for a specific day
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarDayDetailScreen(
    date: String,
    sessionDao: SessionDao,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sessions by sessionDao.getSessionsByDate(date)
        .collectAsState(initial = emptyList())
    
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val displayFormat = remember { SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()) }
    
    val displayDate = remember(date) {
        try {
            displayFormat.format(dateFormat.parse(date) ?: Date())
        } catch (e: Exception) {
            date
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = displayDate,
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
        },
        modifier = modifier
    ) { paddingValues ->
        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = "No sessions",
                        modifier = Modifier.size(64.dp),
                        tint = PushPrimeColors.OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No workouts on this day",
                        style = MaterialTheme.typography.titleMedium,
                        color = PushPrimeColors.OnSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "${sessions.size} session${if (sessions.size > 1) "s" else ""}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(sessions) { session ->
                    SessionCard(session = session)
                }
            }
        }
    }
}

@Composable
fun SessionCard(session: SessionEntity) {
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = PushPrimeColors.Surface
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
                Column {
                    Text(
                        text = formatActivityType(session),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = timeFormat.format(Date(session.startTime)),
                        style = MaterialTheme.typography.bodySmall,
                        color = PushPrimeColors.OnSurfaceVariant
                    )
                }
                Text(
                    text = session.getFormattedDuration(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PushPrimeColors.Primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (session.totalReps != null) {
                    SessionTag("${session.totalReps} reps")
                }
                SessionTag(session.mode)
                SessionTag(session.intensity)
            }
        }
    }
}

private fun formatActivityType(session: SessionEntity): String {
    return when {
        session.activityType.equals("SPORT", ignoreCase = true) -> "Sport"
        session.activityType.equals("QUICK_SESSION", ignoreCase = true) -> "Quick Session"
        else -> "Workout"
    }
}

@Composable
fun SessionTag(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = PushPrimeColors.Primary.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = PushPrimeColors.Primary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
