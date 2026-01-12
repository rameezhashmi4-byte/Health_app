package com.pushprime.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

/**
 * Photo Entry Entity
 * Stores before/after photos locally
 */
@Entity(tableName = "photo_entries")
data class PhotoEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String, // "BEFORE" or "AFTER"
    val uri: String, // Content URI or file path
    val note: String? = null,
    val tags: String? = null, // JSON array or comma-separated
    val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
) {
    /**
     * Get formatted date
     */
    fun getFormattedDate(): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }
}

/**
 * Photo Type enum
 */
enum class PhotoType(val displayName: String) {
    BEFORE("Before"),
    AFTER("After")
}
