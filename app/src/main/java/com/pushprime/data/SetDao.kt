package com.pushprime.data

import androidx.room.*
import com.pushprime.model.SetEntity
import kotlinx.coroutines.flow.Flow

/**
 * Set DAO
 * Data Access Object for SetEntity
 */
@Dao
interface SetDao {
    @Insert
    suspend fun insert(set: SetEntity): Long
    
    @Insert
    suspend fun insertAll(sets: List<SetEntity>)
    
    @Update
    suspend fun update(set: SetEntity)
    
    @Delete
    suspend fun delete(set: SetEntity)
    
    @Query("SELECT * FROM sets WHERE sessionId = :sessionId ORDER BY setNumber ASC")
    fun getSetsBySession(sessionId: Long): Flow<List<SetEntity>>
    
    @Query("SELECT * FROM sets WHERE id = :id")
    suspend fun getSetById(id: Long): SetEntity?
    
    @Query("DELETE FROM sets WHERE sessionId = :sessionId")
    suspend fun deleteSetsBySession(sessionId: Long)
}
