package com.pushprime.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pushprime.network.VoipService
import com.pushprime.ui.theme.PushPrimeColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Group Session Screen
 * VOIP group workout with live rep counts and countdown
 */
@Composable
fun GroupSessionScreen(
    voipService: VoipService,
    onNavigateBack: () -> Unit
) {
    val callState by voipService.callState.collectAsState()
    var countdown by remember { mutableStateOf<Int?>(null) }
    val coroutineScope = rememberCoroutineScope()
    
    // Fake participant data (stubbed)
    val fakeParticipants = remember {
        listOf(
            VoipService.Participant("1", "Alex", 25, true),
            VoipService.Participant("2", "Sam", 18, false),
            VoipService.Participant("3", "Jordan", 15, false)
        )
    }
    
    LaunchedEffect(Unit) {
        voipService.updateParticipants(fakeParticipants)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Video preview area (simulated)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1A1A1A)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = "Video",
                    modifier = Modifier.size(64.dp),
                    tint = Color.White.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Group Workout Session",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
        
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        }
        
        // Countdown overlay
        if (countdown != null && countdown!! > 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${countdown}",
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White
                )
            }
        }
        
        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Color.Black.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                )
                .padding(24.dp)
        ) {
            // Status
            Text(
                text = if (callState.isConnected) "Connected" else "Connecting...",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Live rep counts
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(callState.participants) { participant ->
                    ParticipantCard(participant = participant)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FloatingActionButton(
                    onClick = { voipService.toggleMute() },
                    modifier = Modifier.size(56.dp),
                    containerColor = if (callState.isMuted) PushPrimeColors.Error else PushPrimeColors.SurfaceVariant
                ) {
                    Icon(
                        imageVector = if (callState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = if (callState.isMuted) "Unmute" else "Mute",
                        tint = if (callState.isMuted) Color.White else PushPrimeColors.OnSurfaceVariant
                    )
                }
                
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            countdown = 3
                            voipService.startCountdown(3)
                            for (i in 3 downTo 1) {
                                countdown = i
                                delay(1000)
                            }
                            countdown = null
                        }
                    },
                    modifier = Modifier.size(56.dp),
                    containerColor = PushPrimeColors.Secondary
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Start",
                        tint = Color.White
                    )
                }
                
                FloatingActionButton(
                    onClick = {
                        voipService.leaveSession()
                        onNavigateBack()
                    },
                    modifier = Modifier.size(64.dp),
                    containerColor = PushPrimeColors.Error
                ) {
                    Icon(
                        Icons.Default.CallEnd,
                        contentDescription = "End",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ParticipantCard(participant: VoipService.Participant) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (participant.isLeading) 
                PushPrimeColors.Warning.copy(alpha = 0.2f) 
            else 
                PushPrimeColors.Surface.copy(alpha = 0.9f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (participant.isLeading) {
                    Text(
                        text = "🏆",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                Text(
                    text = participant.username,
                style = MaterialTheme.typography.titleLarge,
                    color = PushPrimeColors.OnSurface
                )
                if (participant.isLeading) {
                    Text(
                        text = "Leading now",
                        style = MaterialTheme.typography.bodySmall,
                        color = PushPrimeColors.Warning
                    )
                }
            }
            Text(
                text = "${participant.pushups}",
                style = MaterialTheme.typography.headlineMedium,
                color = PushPrimeColors.Primary
            )
        }
    }
}
