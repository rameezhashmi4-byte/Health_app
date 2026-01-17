package com.pushprime.ui.screens.streak

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pushprime.model.DailyStatusType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreakDetailsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToQuickSession: () -> Unit,
    viewModel: StreakDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Streak Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Last 30 days",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(12.dp))

            val rows = uiState.days.chunked(7)
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filledRow = if (row.size < 7) row + List(7 - row.size) { null } else row
                    filledRow.forEach { day ->
                        if (day == null) {
                            Spacer(modifier = Modifier.weight(1f).height(46.dp))
                        } else {
                            Surface(
                                color = statusColor(day.status),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(PaddingValues(vertical = 6.dp)),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = day.date.dayOfMonth.toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = statusIcon(day.status),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Workout days grow your streak", style = MaterialTheme.typography.bodyMedium)
            Text("Rest days protect it", style = MaterialTheme.typography.bodyMedium)
            Text("Freeze tokens save you from missed days", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onNavigateToQuickSession,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Save my streak",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun statusColor(status: DailyStatusType): Color {
    return when (status) {
        DailyStatusType.WORKOUT -> Color(0xFF22C55E)
        DailyStatusType.REST -> Color(0xFF93C5FD)
        DailyStatusType.FROZEN -> Color(0xFFBFDBFE)
        DailyStatusType.MISSED -> Color(0xFFFCA5A5)
    }
}

private fun statusIcon(status: DailyStatusType): String {
    return when (status) {
        DailyStatusType.WORKOUT -> "✅"
        DailyStatusType.REST -> "🟦"
        DailyStatusType.FROZEN -> "❄️"
        DailyStatusType.MISSED -> "❌"
    }
}
