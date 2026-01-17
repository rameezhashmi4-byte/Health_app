package com.pushprime.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pushprime.model.PullupMaxTest
import kotlinx.coroutines.flow.Flow

@Dao
interface PullupMaxTestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(test: PullupMaxTest)

    @Query("SELECT * FROM pullup_max_tests ORDER BY dateTime DESC")
    fun getAllTests(): Flow<List<PullupMaxTest>>

    @Query("SELECT * FROM pullup_max_tests ORDER BY dateTime DESC")
    suspend fun getAllTestsOnce(): List<PullupMaxTest>

    @Query("SELECT * FROM pullup_max_tests WHERE dateTime BETWEEN :start AND :end ORDER BY dateTime DESC")
    suspend fun getTestsForRangeOnce(start: Long, end: Long): List<PullupMaxTest>

    @Query("SELECT * FROM pullup_max_tests ORDER BY dateTime DESC LIMIT 1")
    suspend fun getLatestTest(): PullupMaxTest?
}
