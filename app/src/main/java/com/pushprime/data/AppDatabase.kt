package com.pushprime.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pushprime.model.CollageEntity
import com.pushprime.model.ExerciseLog
import com.pushprime.model.PhotoEntryEntity
import com.pushprime.model.SessionEntity
import com.pushprime.model.SetEntity

/**
 * App Database
 * Room database for local storage
 * Unified storage for exercises, sessions, sets, photos, and collages
 * Future: Can sync with Firebase collections
 */
@Database(
    entities = [
        ExerciseLog::class,
        SessionEntity::class,
        SetEntity::class,
        PhotoEntryEntity::class,
        CollageEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseLogDao(): ExerciseLogDao
    abstract fun sessionDao(): SessionDao
    abstract fun setDao(): SetDao
    abstract fun photoEntryDao(): PhotoEntryDao
    abstract fun collageDao(): CollageDao
    
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
