package com.pushprime.ui.screens.pullup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import com.pushprime.ui.components.AppCard
import com.pushprime.ui.components.AppPrimaryButton
import com.pushprime.ui.components.AppSecondaryButton
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
                title = {
                    Text(
                        text = "Pull-Up Tracker",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
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
            AppCard(
                containerColor = Color.Black,
                contentColor = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Current Max Reps",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "${uiState.currentMax}",
                            color = Color.White,
                            style = MaterialTheme.typography.displaySmall
                        )
                        if (uiState.isNewPr) {
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD100))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "New PR",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFFFD100)
                                )
                            }
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    label = "Weekly Total",
                    value = "${uiState.weeklyTotal}",
                    icon = Icons.Default.TrendingUp,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Last Session",
                    value = uiState.lastSessionDate,
                    icon = Icons.Default.FitnessCenter,
                    modifier = Modifier.weight(1f)
                )
            }

            AppPrimaryButton(text = "Log Pull-Up Session", onClick = onLogSession)
            AppSecondaryButton(text = "Test Max Reps", onClick = onTestMax)
            AppSecondaryButton(text = "Start Plan", onClick = onOpenPlan)
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier
    ) {
        Column {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge
            )
            Text(label, color = Color.Gray, style = MaterialTheme.typography.labelSmall)
        }
    }
}
