package com.pushprime.ui.screens.pullup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullupTrackerScreen(
    viewModel: PullupTrackerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onLogSession: () -> Unit,
    onTestMax: () -> Unit,
    onOpenPlan: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Pull-Up Tracker", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                color = Color.Black,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Current Max Reps", color = Color.Gray, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "${uiState.currentMax}",
                            color = Color.White,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black
                        )
                        if (uiState.isNewPr) {
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD100))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("New PR", color = Color(0xFFFFD100), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard(label = "Weekly Total", value = "${uiState.weeklyTotal}", icon = Icons.Default.TrendingUp)
                StatCard(label = "Last Session", value = uiState.lastSessionDate, icon = Icons.Default.FitnessCenter)
            }

            Button(onClick = onLogSession, modifier = Modifier.fillMaxWidth()) {
                Text("Log Pull-Up Session")
            }
            OutlinedButton(onClick = onTestMax, modifier = Modifier.fillMaxWidth()) {
                Text("Test Max Reps")
            }
            OutlinedButton(onClick = onOpenPlan, modifier = Modifier.fillMaxWidth()) {
                Text("Start Plan")
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        color = Color(0xFFF6F6F6),
        modifier = Modifier.weight(1f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.Black)
            Text(label, color = Color.Gray, style = MaterialTheme.typography.labelSmall)
        }
    }
}
