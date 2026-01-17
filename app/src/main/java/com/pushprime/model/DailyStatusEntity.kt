package com.pushprime.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Daily status for streak tracking.
 */
@Entity(tableName = "daily_statuses")
data class DailyStatusEntity(
    @PrimaryKey
    val date: String,
    val status: String,
    val sessionCount: Int
)

enum class DailyStatusType {
    WORKOUT,
    REST,
    MISSED,
    FROZEN
}
