package com.pushprime.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

/**
 * Collage Entity
 * Stores generated collage metadata
 */
@Entity(tableName = "collages")
data class CollageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val title: String? = null,
    val outputUri: String, // Content URI of generated collage
    val photoIds: String, // JSON array of photo IDs used
    val layoutTemplate: String = "2_UP_VERTICAL", // Template name
    val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
) {
    /**
     * Get formatted date
     */
    fun getFormattedDate(): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return dateFormat.format(Date(createdAt))
    }
}

/**
 * Collage Layout Template enum
 */
enum class CollageLayout(val displayName: String, val photoCount: Int) {
    TWO_UP_VERTICAL("2-Up Vertical", 2),
    TWO_BY_TWO_GRID("2x2 Grid", 4),
    THREE_COLUMN_STRIP("3-Column Strip", 3),
    SIX_GRID("6-Grid", 6)
}
