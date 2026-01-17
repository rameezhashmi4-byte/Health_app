package com.pushprime.ui.components

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pushprime.ui.theme.PushPrimeColors

@Composable
fun WorkoutMusicBar(
    isPlaying: Boolean,
    currentTrack: String?,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onPresetSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPreset by remember { mutableStateOf("High Energy") }
    val presets = listOf("Chill", "High Energy", "Beast Mode")

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        color = Color.Black.copy(alpha = 0.9f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Track Info & Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SPOTIFY CONNECTED",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF1DB954) // Spotify Green
                    )
                    Text(
                        text = currentTrack ?: "No Track Playing",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        maxLines = 1
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPrevious) {
                        Icon(Icons.Default.SkipPrevious, "Prev", tint = Color.White)
                    }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { onPlayPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.Black
                        )
                    }
                    IconButton(onClick = onNext) {
                        Icon(Icons.Default.SkipNext, "Next", tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Energy Presets
            Text(
                text = "ENERGY PRESETS",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { preset ->
                    val isSelected = preset == selectedPreset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) PushPrimeColors.GTAYellow else Color.DarkGray)
                            .clickable {
                                selectedPreset = preset
                                onPresetSelected(preset)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = preset,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) Color.Black else Color.White
                        )
                    }
                }
            }
        }
    }
}
