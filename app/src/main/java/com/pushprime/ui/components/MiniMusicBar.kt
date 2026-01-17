package com.pushprime.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pushprime.ui.theme.PushPrimeColors

@Composable
fun MiniMusicBar(
    isConnected: Boolean,
    trackName: String?,
    artistName: String?,
    isPlaying: Boolean,
    onConnect: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PushPrimeColors.Surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isConnected) {
                Text(
                    text = "Connect Spotify to play music",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PushPrimeColors.OnSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Button(onClick = onConnect) {
                    Text(
                        text = "Connect",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            } else {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = PushPrimeColors.Primary
                    )
                    Text(
                        text = trackName ?: "Now playing",
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPrevious) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Previous")
                    }
                    IconButton(onClick = onPlayPause) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play"
                        )
                    }
                    IconButton(onClick = onNext) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Next")
                    }
                }
            }
        }
        if (isConnected && !artistName.isNullOrBlank()) {
            Text(
                text = artistName,
                style = MaterialTheme.typography.labelSmall,
                color = PushPrimeColors.OnSurfaceVariant,
                modifier = Modifier.padding(start = 52.dp, bottom = 12.dp)
            )
        }
    }
}
