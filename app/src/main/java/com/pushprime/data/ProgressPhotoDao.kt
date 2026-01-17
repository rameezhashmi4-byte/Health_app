package com.pushprime.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pushprime.model.ProgressPhotoEntity
import kotlinx.coroutines.flow.Flow

/**
 * Progress photo DAO.
 */
@Dao
interface ProgressPhotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(photo: ProgressPhotoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(photos: List<ProgressPhotoEntity>)

    @Query("SELECT * FROM progress_photos WHERE uid = :uid ORDER BY takenAt DESC")
    fun getPhotosForUser(uid: String): Flow<List<ProgressPhotoEntity>>

    @Query("SELECT * FROM progress_photos WHERE uid = :uid")
    suspend fun getPhotosForUserOnce(uid: String): List<ProgressPhotoEntity>

    @Query("SELECT * FROM progress_photos WHERE photoId = :photoId LIMIT 1")
    suspend fun getPhotoById(photoId: String): ProgressPhotoEntity?

    @Query("SELECT * FROM progress_photos WHERE photoId = :photoId LIMIT 1")
    fun observePhotoById(photoId: String): Flow<ProgressPhotoEntity?>

    @Query("SELECT * FROM progress_photos WHERE syncStatus != :syncedStatus")
    suspend fun getPhotosNeedingSync(syncedStatus: String): List<ProgressPhotoEntity>

    @Query(
        "UPDATE progress_photos SET syncStatus = :status, downloadUrl = :downloadUrl, " +
            "storagePath = :storagePath, lastSyncAt = :lastSyncAt, syncAttempts = :syncAttempts " +
            "WHERE photoId = :photoId"
    )
    suspend fun updateSyncState(
        photoId: String,
        status: String,
        downloadUrl: String?,
        storagePath: String,
        lastSyncAt: Long?,
        syncAttempts: Int
    )

    @Query("DELETE FROM progress_photos WHERE photoId = :photoId")
    suspend fun deleteById(photoId: String)
}
