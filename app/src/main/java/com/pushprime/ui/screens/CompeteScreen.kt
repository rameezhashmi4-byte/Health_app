package com.pushprime.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pushprime.data.FirebaseHelper
import com.pushprime.data.LocalStore
import com.pushprime.model.LeaderboardEntry
import com.pushprime.ui.components.LeaderboardCard
import com.pushprime.ui.components.PushPrimeSpacing
import com.pushprime.ui.theme.PushPrimeColors
import kotlinx.coroutines.launch

/**
 * Compete Screen
 * Local and global leaderboards with toggle
 */
@Composable
fun CompeteScreen(
    localStore: LocalStore,
    firebaseHelper: FirebaseHelper,
    onNavigateBack: () -> Unit
) {
    var showLocal by remember { mutableStateOf(true) }
    var localLeaderboard by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
    var globalLeaderboard by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(showLocal) {
        if (showLocal) {
            localLeaderboard = localStore.getLocalLeaderboard()
        } else {
            isLoading = true
            coroutineScope.launch {
                globalLeaderboard = firebaseHelper.getGlobalLeaderboard(limit = 100)
                isLoading = false
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
                text = "Leaderboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = PushPrimeColors.OnSurface
            )
        }
        
        // Toggle Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilterChip(
                selected = showLocal,
                onClick = { showLocal = true },
                label = { Text("Friends") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp)
            )
            FilterChip(
                selected = !showLocal,
                onClick = { showLocal = false },
                label = { Text("Global") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp)
            )
        }
        
        // Leaderboard List
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PushPrimeColors.Primary)
            }
        } else {
            val leaderboard = if (showLocal) localLeaderboard else globalLeaderboard
            
            if (leaderboard.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No entries yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = PushPrimeColors.OnSurfaceVariant
                        )
                        Text(
                            text = "Complete a workout to appear on the leaderboard!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PushPrimeColors.OnSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(leaderboard) { entry ->
                        LeaderboardCard(entry = entry)
                    }
                }
            }
        }
    }
}
