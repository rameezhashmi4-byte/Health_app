package com.pushprime.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.pushprime.ui.theme.PushPrimeColors

/**
 * Stories Row Component
 * Instagram-like horizontal scrollable stories
 */
@Composable
fun StoriesRow(
    onStoryClick: (StoryType) -> Unit,
    modifier: Modifier = Modifier
) {
    val stories = listOf(
        StoryItem(StoryType.QUICK_START, "Quick Start", "🚀"),
        StoryItem(StoryType.TODAY_PLAN, "Today Plan", "📋"),
        StoryItem(StoryType.SPORTS, "Sports", "⚽"),
        StoryItem(StoryType.PROGRESS, "Progress", "📊"),
        StoryItem(StoryType.BEFORE_AFTER, "Before/After", "📸")
    )
    
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(stories) { story ->
            StoryCircle(
                story = story,
                onClick = { onStoryClick(story.type) },
                modifier = Modifier.size(72.dp)
            )
        }
    }
}

/**
 * Story Circle Component
 */
@Composable
fun StoryCircle(
    story: StoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(60.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            shadowElevation = 1.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, PushPrimeColors.Outline)
        ) {
            Box(contentAlignment = Alignment.Center) {
            Text(
                text = story.emoji,
                style = MaterialTheme.typography.headlineSmall
            )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = story.label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Story Item
 */
data class StoryItem(
    val type: StoryType,
    val label: String,
    val emoji: String
)

/**
 * Story Type
 */
enum class StoryType {
    QUICK_START,
    TODAY_PLAN,
    SPORTS,
    PROGRESS,
    BEFORE_AFTER
}
