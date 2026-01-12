package com.pushprime.data

import androidx.room.*
import com.pushprime.model.CollageEntity
import kotlinx.coroutines.flow.Flow

/**
 * Collage DAO
 * Data Access Object for CollageEntity
 */
@Dao
interface CollageDao {
    @Insert
    suspend fun insert(collage: CollageEntity): Long
    
    @Update
    suspend fun update(collage: CollageEntity)
    
    @Delete
    suspend fun delete(collage: CollageEntity)
    
    @Query("SELECT * FROM collages ORDER BY createdAt DESC")
    fun getAllCollages(): Flow<List<CollageEntity>>
    
    @Query("SELECT * FROM collages WHERE id = :id")
    suspend fun getCollageById(id: Long): CollageEntity?
    
    @Query("SELECT * FROM collages WHERE date = :date ORDER BY createdAt DESC")
    fun getCollagesByDate(date: String): Flow<List<CollageEntity>>
}
