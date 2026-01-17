package com.pushprime.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Quick Session log
 * Stored locally to track template usage and notes.
 */
@Entity(tableName = "quick_session_logs")
data class QuickSessionLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateTime: Long = System.currentTimeMillis(),
    val templateId: String,
    val durationMinutes: Int = 10,
    val completed: Boolean = true,
    val notes: String? = null
)
