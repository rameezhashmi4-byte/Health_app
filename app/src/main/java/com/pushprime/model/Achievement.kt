package com.pushprime.model

enum class AchievementType {
    STREAK,
    SESSIONS,
    SPORTS,
    STEPS
}

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val type: AchievementType,
    val threshold: Int,
    val progress: Int,
    val unlocked: Boolean,
    val unlockedAt: Long?,
    val icon: String
)
