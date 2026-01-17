package com.pushprime.model

import java.util.UUID

data class SportsSession(
    val id: String = UUID.randomUUID().toString(),
    val sportType: SportType,
    val startTime: Long,
    val endTime: Long,
    val durationMinutes: Int,
    val effortLevel: Intensity,
    val intervalsEnabled: Boolean,
    val warmupEnabled: Boolean,
    val notes: String,
    val rating: Int,
    val caloriesEstimate: Int
)
