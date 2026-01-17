package com.pushprime.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Progress collage metadata stored locally.
 */
@Entity(tableName = "progress_collages")
data class ProgressCollageEntity(
    @PrimaryKey
    val collageId: String,
    val uid: String,
    val photoIds: String,
    val createdAt: Long,
    val localPath: String?,
    val storagePath: String,
    val downloadUrl: String? = null,
    val syncStatus: String = SyncStatus.PENDING.name,
    val syncAttempts: Int = 0,
    val lastSyncAt: Long? = null
)
