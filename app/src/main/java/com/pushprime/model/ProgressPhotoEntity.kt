package com.pushprime.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Progress photo metadata stored locally.
 */
@Entity(tableName = "progress_photos")
data class ProgressPhotoEntity(
    @PrimaryKey
    val photoId: String,
    val uid: String,
    val poseTag: String,
    val notes: String? = null,
    val takenAt: Long,
    val createdAt: Long,
    val localPath: String?,
    val storagePath: String,
    val downloadUrl: String? = null,
    val thumbUrl: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val syncStatus: String = SyncStatus.PENDING.name,
    val syncAttempts: Int = 0,
    val lastSyncAt: Long? = null
)

enum class PoseTag(val displayName: String) {
    FRONT("Front"),
    SIDE("Side"),
    BACK("Back");

    companion object {
        fun from(raw: String?): PoseTag {
            return values().firstOrNull { it.name == raw } ?: FRONT
        }
    }
}

enum class SyncStatus {
    PENDING,
    SYNCED,
    FAILED;

    companion object {
        fun from(raw: String?): SyncStatus {
            return values().firstOrNull { it.name == raw } ?: PENDING
        }
    }
}
