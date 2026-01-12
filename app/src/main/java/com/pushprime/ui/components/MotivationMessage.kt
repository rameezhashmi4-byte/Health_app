package com.pushprime.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pushprime.ui.theme.PushPrimeColors

/**
 * Motivation Message Component
 * Shows personalized motivation based on progress
 */
@Composable
fun MotivationMessage(
    currentWeekTotal: Int,
    previousWeekTotal: Int,
    streak: Int,
    modifier: Modifier = Modifier
) {
    val message = generateMotivationMessage(currentWeekTotal, previousWeekTotal, streak)
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = message.color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message.emoji,
                style = MaterialTheme.typography.displaySmall
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = PushPrimeColors.OnSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message.message,
                style = MaterialTheme.typography.bodyLarge,
                color = PushPrimeColors.OnSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun generateMotivationMessage(
    currentWeek: Int,
    previousWeek: Int,
    streak: Int
): MotivationData {
    val change = currentWeek - previousWeek
    val percentChange = if (previousWeek > 0) {
        ((change.toFloat() / previousWeek) * 100).toInt()
    } else if (currentWeek > 0) {
        100
    } else {
        0
    }
    
    return when {
        // Excellent progress
        change > 0 && percentChange >= 50 -> MotivationData(
            emoji = "🚀",
            title = "Outstanding Progress!",
            message = "You've increased your activity by ${percentChange}%! You're on fire!",
            color = PushPrimeColors.Success
        )
        
        // Good progress
        change > 0 && percentChange >= 20 -> MotivationData(
            emoji = "💪",
            title = "Great Improvement!",
            message = "You're ${percentChange}% more active than last week. Keep pushing!",
            color = PushPrimeColors.Success
        )
        
        // Steady progress
        change > 0 -> MotivationData(
            emoji = "📈",
            title = "Moving Forward!",
            message = "You're doing ${change} more reps than last week. Every step counts!",
            color = PushPrimeColors.Primary
        )
        
        // Streak motivation
        streak >= 7 -> MotivationData(
            emoji = "🔥",
            title = "Incredible Streak!",
            message = "You've worked out ${streak} days in a row! You're unstoppable!",
            color = PushPrimeColors.Warning
        )
        
        // Consistent
        streak >= 3 -> MotivationData(
            emoji = "⭐",
            title = "Building Consistency!",
            message = "You're on a ${streak}-day streak! Keep the momentum going!",
            color = PushPrimeColors.Primary
        )
        
        // Starting out
        currentWeek > 0 && previousWeek == 0 -> MotivationData(
            emoji = "🎯",
            title = "Great Start!",
            message = "You've logged ${currentWeek} reps this week. Every journey begins with a single step!",
            color = PushPrimeColors.Primary
        )
        
        // Needs improvement
        change < 0 && percentChange <= -20 -> MotivationData(
            emoji = "💭",
            title = "Time to Refocus",
            message = "You're ${-percentChange}% below last week. Remember why you started - you've got this!",
            color = PushPrimeColors.Error
        )
        
        // Slight dip
        change < 0 -> MotivationData(
            emoji = "🌱",
            title = "Small Setback",
            message = "You're ${-change} reps below last week. Tomorrow is a new day to bounce back!",
            color = PushPrimeColors.Primary
        )
        
        // No activity
        else -> MotivationData(
            emoji = "🌟",
            title = "Ready to Start?",
            message = "Log your first workout and begin your fitness journey today!",
            color = PushPrimeColors.OnSurfaceVariant
        )
    }
}

private data class MotivationData(
    val emoji: String,
    val title: String,
    val message: String,
    val color: androidx.compose.ui.graphics.Color
)
