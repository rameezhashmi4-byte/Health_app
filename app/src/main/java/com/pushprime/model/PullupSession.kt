package com.pushprime.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "pullup_sessions")
data class PullupSession(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val dateTime: Long = System.currentTimeMillis(),
    val repsBySet: List<Int> = emptyList(),
    val totalReps: Int = 0,
    val addedWeightKg: Double? = null,
    val restSeconds: Int? = null,
    val notes: String? = null,
    val volumeScore: Int = 0
)
