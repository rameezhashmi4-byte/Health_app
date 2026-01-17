package com.pushprime.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pushprime.model.ProgressCollageEntity
import kotlinx.coroutines.flow.Flow

/**
 * Progress collage DAO.
 */
@Dao
interface ProgressCollageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(collage: ProgressCollageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(collages: List<ProgressCollageEntity>)

    @Query("SELECT * FROM progress_collages WHERE uid = :uid ORDER BY createdAt DESC")
    fun getCollagesForUser(uid: String): Flow<List<ProgressCollageEntity>>

    @Query("SELECT * FROM progress_collages WHERE collageId = :collageId LIMIT 1")
    suspend fun getCollageById(collageId: String): ProgressCollageEntity?

    @Query("SELECT * FROM progress_collages WHERE syncStatus != :syncedStatus")
    suspend fun getCollagesNeedingSync(syncedStatus: String): List<ProgressCollageEntity>

    @Query(
        "UPDATE progress_collages SET syncStatus = :status, downloadUrl = :downloadUrl, " +
            "storagePath = :storagePath, lastSyncAt = :lastSyncAt, syncAttempts = :syncAttempts " +
            "WHERE collageId = :collageId"
    )
    suspend fun updateSyncState(
        collageId: String,
        status: String,
        downloadUrl: String?,
        storagePath: String,
        lastSyncAt: Long?,
        syncAttempts: Int
    )

    @Query("DELETE FROM progress_collages WHERE collageId = :collageId")
    suspend fun deleteById(collageId: String)
}
