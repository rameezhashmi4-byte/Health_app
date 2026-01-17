package com.pushprime.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pushprime.ui.theme.PushPrimeColors

@Composable
fun CoachControlBar(
    isCoachEnabled: Boolean,
    isMuted: Boolean,
    onMuteToggle: () -> Unit,
    onSaySomething: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = PushPrimeColors.Surface,
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 2.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (isCoachEnabled) "🎙 Coach ON" else "🎙 Coach OFF",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onMuteToggle) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = if (isMuted) "Unmute coach" else "Mute coach",
                        tint = if (isMuted) Color.Gray else PushPrimeColors.Primary
                    )
                }
                Spacer(modifier = Modifier.size(6.dp))
                Button(
                    onClick = onSaySomething,
                    colors = ButtonDefaults.buttonColors(containerColor = PushPrimeColors.Primary)
                ) {
                    Text("Say something")
                }
            }
        }
    }
}
