package com.pushprime.data

import androidx.room.*
import com.pushprime.model.PhotoEntryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Photo Entry DAO
 * Data Access Object for PhotoEntryEntity
 */
@Dao
interface PhotoEntryDao {
    @Insert
    suspend fun insert(photo: PhotoEntryEntity): Long
    
    @Update
    suspend fun update(photo: PhotoEntryEntity)
    
    @Delete
    suspend fun delete(photo: PhotoEntryEntity)
    
    @Query("SELECT * FROM photo_entries ORDER BY timestamp DESC")
    fun getAllPhotos(): Flow<List<PhotoEntryEntity>>
    
    @Query("SELECT * FROM photo_entries WHERE type = :type ORDER BY timestamp DESC")
    fun getPhotosByType(type: String): Flow<List<PhotoEntryEntity>>
    
    @Query("SELECT * FROM photo_entries WHERE id = :id")
    suspend fun getPhotoById(id: Long): PhotoEntryEntity?
    
    @Query("SELECT * FROM photo_entries WHERE date = :date ORDER BY timestamp DESC")
    fun getPhotosByDate(date: String): Flow<List<PhotoEntryEntity>>
    
    @Query("SELECT * FROM photo_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    fun getPhotosByDateRange(startDate: String, endDate: String): Flow<List<PhotoEntryEntity>>
    
    @Query("SELECT COUNT(*) FROM photo_entries WHERE type = :type")
    suspend fun getPhotoCountByType(type: String): Int
}
