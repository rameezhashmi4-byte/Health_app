package com.pushprime.model

/**
 * Streak state persisted locally.
 */
data class StreakState(
    val currentStreakDays: Int = 0,
    val longestStreakDays: Int = 0,
    val lastWorkoutDate: String = "",
    val lastStreakProtectedDate: String? = null,
    val freezeTokensRemaining: Int = 2,
    val restDaysUsedThisWeek: Int = 0,
    val lastTokenResetMonth: String = ""
)
