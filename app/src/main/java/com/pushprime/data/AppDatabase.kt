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
    version = 4,
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
                    .addMigrations(MIGRATION_3_4)
                    .fallbackToDestructiveMigration() // Keep safety net
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE sessions ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE sessions ADD COLUMN lastSyncedAt INTEGER")
                database.execSQL("ALTER TABLE sessions ADD COLUMN syncAttempts INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
