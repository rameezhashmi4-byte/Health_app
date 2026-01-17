package com.pushprime.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pushprime.ui.theme.PushPrimeColors

@Composable
fun VoiceCoachStatusBadge(
    isEnabled: Boolean,
    isMuted: Boolean,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isEnabled) return
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = PushPrimeColors.Surface.copy(alpha = 0.9f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (isMuted) "\uD83C\uDF99 Voice Coach Muted" else "\uD83C\uDF99 Voice Coach ON",
                style = MaterialTheme.typography.labelMedium,
                color = PushPrimeColors.OnSurface
            )
            Spacer(modifier = Modifier.size(4.dp))
            TextButton(onClick = onToggleMute) {
                Text(
                    text = if (isMuted) "Unmute" else "Mute",
                    style = MaterialTheme.typography.labelSmall,
                    color = PushPrimeColors.Primary
                )
            }
        }
    }
}
