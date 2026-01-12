package com.pushprime.data

/**
 * Data class for daily summary query result
 * Used by Room DAO for aggregate queries
 */
data class DailySummaryResult(
    val exerciseName: String,
    val total: Long // SUM returns Long in SQLite
)
