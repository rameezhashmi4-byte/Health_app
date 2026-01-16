package com.pushprime.model

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val condition: (AchievementProgress) -> Boolean,
    val isUnlocked: Boolean = false,
    val unlockDate: Long? = null
)

data class AchievementProgress(
    val totalPushups: Int,
    val totalWorkouts: Int,
    val maxPushupsInOneSession: Int,
    val currentStreak: Int,
    val totalDaysActive: Int
)

object AchievementList {
    val ACHIEVEMENTS = listOf(
        Achievement(
            id = "first_step",
            title = "First Step",
            description = "Complete your first workout session",
            iconEmoji = "👟",
            condition = { it.totalWorkouts >= 1 }
        ),
        Achievement(
            id = "pushup_rookie",
            title = "Push-up Rookie",
            description = "Do 100 total push-ups",
            iconEmoji = "🥉",
            condition = { it.totalPushups >= 100 }
        ),
        Achievement(
            id = "pushup_pro",
            title = "Push-up Pro",
            description = "Do 1,000 total push-ups",
            iconEmoji = "🥈",
            condition = { it.totalPushups >= 1000 }
        ),
        Achievement(
            id = "pushup_king",
            title = "Push-up King",
            description = "Do 10,000 total push-ups",
            iconEmoji = "🥇",
            condition = { it.totalPushups >= 10000 }
        ),
        Achievement(
            id = "streak_3",
            title = "Consistent",
            description = "Maintain a 3-day workout streak",
            iconEmoji = "🔥",
            condition = { it.currentStreak >= 3 }
        ),
        Achievement(
            id = "streak_7",
            title = "Unstoppable",
            description = "Maintain a 7-day workout streak",
            iconEmoji = "⚡",
            condition = { it.currentStreak >= 7 }
        ),
        Achievement(
            id = "powerhouse",
            title = "Powerhouse",
            description = "Do 50 push-ups in a single session",
            iconEmoji = "💪",
            condition = { it.maxPushupsInOneSession >= 50 }
        ),
        Achievement(
            id = "beast_mode",
            title = "Beast Mode",
            description = "Do 100 push-ups in a single session",
            iconEmoji = "🦍",
            condition = { it.maxPushupsInOneSession >= 100 }
        )
    )
}
