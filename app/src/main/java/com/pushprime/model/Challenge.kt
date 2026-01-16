package com.pushprime.model

import java.util.*

data class Challenge(
    val id: String,
    val title: String,
    val description: String,
    val targetType: ChallengeTargetType,
    val targetValue: Int,
    val startDate: Long,
    val endDate: Long,
    val participantsCount: Int = 0,
    val isActive: Boolean = true
)

enum class ChallengeTargetType {
    TOTAL_PUSHUPS,
    CONSECUTIVE_DAYS,
    WORKOUT_MINUTES
}

object WeeklyChallenges {
    fun getCurrentChallenge(): Challenge {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val startDate = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        val endDate = calendar.timeInMillis

        return Challenge(
            id = "weekly_pushup_blitz",
            title = "WEEKLY PUSH-UP BLITZ",
            description = "Complete 500 push-ups this week to earn the Blitz badge!",
            targetType = ChallengeTargetType.TOTAL_PUSHUPS,
            targetValue = 500,
            startDate = startDate,
            endDate = endDate,
            participantsCount = 1243
        )
    }
}
