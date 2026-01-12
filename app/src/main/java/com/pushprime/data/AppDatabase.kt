package com.pushprime.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pushprime.model.ExerciseLog

/**
 * App Database
 * Room database for local storage
 * Future: Can sync with Firebase "exercise_logs" collection
 */
@Database(
    entities = [ExerciseLog::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseLogDao(): ExerciseLogDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pushprime_database"
                )
                    .fallbackToDestructiveMigration() // For MVP - allows schema changes
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
