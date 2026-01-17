package com.pushprime.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pushprime.music.EnergyLevel
import com.pushprime.music.MusicPhase
import com.pushprime.music.MusicPhaseLogic
import com.pushprime.music.MusicProvider
import com.pushprime.music.MusicSource
import com.pushprime.ui.theme.PushPrimeColors
import kotlinx.coroutines.delay

@Composable
fun MusicModeOverlay(
    elapsedSeconds: Int,
    expectedDurationSeconds: Int,
    baseBpm: Int,
    energyLevel: EnergyLevel,
    musicSource: MusicSource,
    autopilotEnabled: Boolean,
    onAutopilotToggle: (Boolean) -> Unit,
    musicProvider: MusicProvider,
    phaseHint: MusicPhase? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var boostActive by remember { mutableStateOf(false) }
    var manualPhase by remember { mutableStateOf(MusicPhase.MAIN) }

    val autoPhase = remember(elapsedSeconds, expectedDurationSeconds, phaseHint) {
        MusicPhaseLogic.resolvePhase(
            elapsedSeconds = elapsedSeconds,
            totalDurationSeconds = expectedDurationSeconds,
            phaseHint = phaseHint
        )
    }

    LaunchedEffect(autopilotEnabled, autoPhase) {
        if (autopilotEnabled) {
            manualPhase = autoPhase
        }
    }

    LaunchedEffect(boostActive) {
        if (boostActive) {
            delay(30_000)
            boostActive = false
        }
    }

    val currentPhase = if (autopilotEnabled) autoPhase else manualPhase
    val boostBpm = if (boostActive) 15 else 0
    val targetBpm = MusicPhaseLogic.targetBpmForPhase(baseBpm, currentPhase, boostBpm)
    val bpmRange = MusicPhaseLogic.bpmRangeForTarget(targetBpm)
    val suggestedTrackType = MusicPhaseLogic.suggestedTrackType(currentPhase)

    val isConnected by musicProvider.isConnected.collectAsState(initial = false)
    val currentTrack by musicProvider.currentTrack.collectAsState(initial = null)

    LaunchedEffect(currentPhase, energyLevel, bpmRange) {
        musicProvider.setEnergyLevel(energyLevel)
        musicProvider.setTargetBpm(bpmRange)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PushPrimeColors.Surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Music Mode",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${currentPhase.label} • ${bpmRange.first}–${bpmRange.last} BPM",
                        style = MaterialTheme.typography.bodySmall,
                        color = PushPrimeColors.OnSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (autopilotEnabled) "Autopilot ON" else "Autopilot OFF",
                        style = MaterialTheme.typography.labelSmall,
                        color = PushPrimeColors.OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Switch(checked = autopilotEnabled, onCheckedChange = onAutopilotToggle)
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PushPrimeColors.Background
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Suggested BPM",
                            style = MaterialTheme.typography.labelSmall,
                            color = PushPrimeColors.OnSurfaceVariant
                        )
                        Text(
                            text = "${bpmRange.first}–${bpmRange.last}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Track type",
                            style = MaterialTheme.typography.labelSmall,
                            color = PushPrimeColors.OnSurfaceVariant
                        )
                        Text(
                            text = suggestedTrackType,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { boostActive = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = PushPrimeColors.GTAYellow),
                    enabled = !boostActive
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(if (boostActive) "Boosting +15 BPM" else "Energy Boost")
                }
                Button(
                    onClick = { musicProvider.skipNext() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = PushPrimeColors.Primary)
                ) {
                    Icon(Icons.Default.SkipNext, contentDescription = null)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Skip")
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = PushPrimeColors.SurfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(PushPrimeColors.Primary.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = PushPrimeColors.Primary
                            )
                        }
                        Spacer(modifier = Modifier.size(10.dp))
                        Column {
                            Text(
                                text = "Now Playing",
                                style = MaterialTheme.typography.labelSmall,
                                color = PushPrimeColors.OnSurfaceVariant
                            )
                            Text(
                                text = currentTrack?.title ?: "Suggested mix ready",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Text(
                        text = if (isConnected) "Connected" else "Offline",
                        style = MaterialTheme.typography.labelSmall,
                        color = PushPrimeColors.OnSurfaceVariant
                    )
                }
            }

            if (musicSource == MusicSource.BASIC) {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://open.spotify.com"))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PushPrimeColors.UberBlack)
                ) {
                    Text("Open Spotify")
                }
            }
        }
    }
}
