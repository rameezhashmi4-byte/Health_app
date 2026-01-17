package com.pushprime.ui.screens.social

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.pushprime.model.WeeklyChallenges
import com.pushprime.model.ChallengeTargetType

/**
 * Compete Screen
 * Weekly Challenges + Global/Local Leaderboards
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompeteScreen(
    localStore: LocalStore,
    firebaseHelper: FirebaseHelper?,
    onNavigateBack: () -> Unit
) {
    var showLocal by remember { mutableStateOf(false) }
    var localLeaderboard by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
    var globalLeaderboard by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val weeklyChallenge = remember { WeeklyChallenges.getCurrentChallenge() }
    
    LaunchedEffect(showLocal) {
        if (showLocal) {
            localLeaderboard = localStore.getLocalLeaderboard()
        } else {
            if (firebaseHelper?.isAvailable == true) {
                isLoading = true
                coroutineScope.launch {
                    globalLeaderboard = firebaseHelper.getGlobalLeaderboard(limit = 100)
                    isLoading = false
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "COMPETE",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Weekly Challenge Section
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    color = Color.Black,
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "WEEKLY CHALLENGE",
                            style = MaterialTheme.typography.labelLarge,
                            color = PushPrimeColors.GTAYellow,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = weeklyChallenge.title,
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = weeklyChallenge.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Progress Bar for Challenge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "120 / ${weeklyChallenge.targetValue}",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "24% COMPLETE",
                                style = MaterialTheme.typography.labelSmall,
                                color = PushPrimeColors.GTAGreen,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = 0.24f,
                            modifier = Modifier.fillMaxWidth().height(4.dp),
                            color = PushPrimeColors.GTAGreen,
                            trackColor = Color.DarkGray
                        )
                    }
                }
            }

            // Leaderboard Toggle
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .background(if (!showLocal) Color.Black else Color.LightGray)
                            .clickable { showLocal = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "GLOBAL",
                            fontWeight = FontWeight.Black,
                            color = if (!showLocal) Color.White else Color.Black
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .background(if (showLocal) Color.Black else Color.LightGray)
                            .clickable { showLocal = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "FRIENDS",
                            fontWeight = FontWeight.Black,
                            color = if (showLocal) Color.White else Color.Black
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Leaderboard Items
            val leaderboard = if (showLocal) localLeaderboard else globalLeaderboard
            if (isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.Black)
                    }
                }
            } else if (leaderboard.isEmpty()) {
                item {
                    Text(
                        "No rankings yet. Start a session!",
                        modifier = Modifier.padding(24.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            } else {
                items(leaderboard) { entry ->
                    LeaderboardCard(entry = entry)
                }
            }
        }
    }
}
