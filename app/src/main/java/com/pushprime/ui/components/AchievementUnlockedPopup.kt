package com.pushprime.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pushprime.model.Achievement
import com.pushprime.ui.theme.PushPrimeColors

@Composable
fun AchievementUnlockedPopup(
    achievement: Achievement?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = achievement != null,
        enter = fadeIn() + scaleIn(initialScale = 0.9f),
        exit = fadeOut() + scaleOut(targetScale = 0.95f)
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.2f)),
            contentAlignment = Alignment.TopCenter
        ) {
            Card(
                modifier = Modifier
                    .padding(top = 32.dp)
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PushPrimeColors.Surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🔥 Achievement Unlocked!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(
                        text = achievement?.icon.orEmpty(),
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = achievement?.title.orEmpty(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = "Nice work — keep going 💪",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PushPrimeColors.OnSurfaceVariant
                    )
                }
            }
        }
    }
}
