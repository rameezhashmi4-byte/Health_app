package com.pushprime.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pushprime.ui.theme.PushPrimeColors

/**
 * Push-Up Counter Component
 * Large, tap-friendly counter with timer and controls
 */
@Composable
fun PushUpCounter(
    pushupCount: Int,
    isActive: Boolean,
    elapsedTime: Long, // in seconds
    onIncrement: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp), // 2xl corners
        colors = CardDefaults.cardColors(
            containerColor = PushPrimeColors.Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Push-Up Counter",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = PushPrimeColors.OnSurface
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Timer
            Text(
                text = formatTime(elapsedTime),
                style = MaterialTheme.typography.headlineMedium,
                color = PushPrimeColors.OnSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // Counter with animation
            val scale = animateFloatAsState(
                targetValue = if (pushupCount > 0) 1.15f else 1f,
                animationSpec = tween(300),
                label = "counterScale"
            )
            
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(scale.value)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PushPrimeColors.Primary.copy(alpha = 0.25f),
                                PushPrimeColors.Primary.copy(alpha = 0.05f)
                            )
                        )
                    )
                    .clickable(enabled = isActive) { onIncrement() },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$pushupCount",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = PushPrimeColors.Primary
                    )
                    Text(
                        text = if (pushupCount == 1) "push-up" else "push-ups",
                        style = MaterialTheme.typography.bodyLarge,
                        color = PushPrimeColors.OnSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // Increment button (only when active)
            if (isActive) {
                Button(
                    onClick = onIncrement,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PushPrimeColors.Primary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Tap to Add Push-Up",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onReset,
                    enabled = pushupCount > 0 || isActive,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset")
                }
                
                Button(
                    onClick = if (isActive) onStop else onStart,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) PushPrimeColors.Error else PushPrimeColors.Secondary
                    )
                ) {
                    Icon(
                        imageVector = if (isActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isActive) "Stop" else "Start"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isActive) "Stop" else "Start")
                }
            }
        }
    }
}

fun formatTime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format("%d:%02d", minutes, secs)
    }
}
