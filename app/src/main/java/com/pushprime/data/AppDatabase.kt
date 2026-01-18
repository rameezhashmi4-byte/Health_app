package com.pushprime.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pushprime.model.AchievementEntity
import com.pushprime.model.CollageEntity
import com.pushprime.model.DailyStatusEntity
import com.pushprime.model.ExerciseLog
import com.pushprime.model.GeneratedWorkoutPlanEntity
import com.pushprime.model.PhotoEntryEntity
import com.pushprime.model.PullupMaxTest
import com.pushprime.model.PullupSession
import com.pushprime.model.NutritionEntry
import com.pushprime.model.ProgressCollageEntity
import com.pushprime.model.ProgressPhotoEntity
import com.pushprime.model.QuickSessionLog
import com.pushprime.model.SessionEntity
import com.pushprime.model.SetEntity
import com.pushprime.model.WorkoutSession

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
        CollageEntity::class,
        ProgressPhotoEntity::class,
        ProgressCollageEntity::class,
        AchievementEntity::class,
        DailyStatusEntity::class,
        GeneratedWorkoutPlanEntity::class,
        QuickSessionLog::class,
        NutritionEntry::class,
        PullupSession::class,
        PullupMaxTest::class,
        WorkoutSession::class
    ],
    version = 13,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseLogDao(): ExerciseLogDao
    abstract fun sessionDao(): SessionDao
    abstract fun setDao(): SetDao
    abstract fun photoEntryDao(): PhotoEntryDao
    abstract fun collageDao(): CollageDao
    abstract fun progressPhotoDao(): ProgressPhotoDao
    abstract fun progressCollageDao(): ProgressCollageDao
    abstract fun achievementDao(): AchievementDao
    abstract fun dailyStatusDao(): DailyStatusDao
    abstract fun workoutPlanDao(): WorkoutPlanDao
    abstract fun quickSessionDao(): QuickSessionDao
    abstract fun nutritionDao(): NutritionDao
    abstract fun pullupSessionDao(): PullupSessionDao
    abstract fun pullupMaxTestDao(): PullupMaxTestDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    
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
                    .addMigrations(
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8,
                        MIGRATION_8_9,
                        MIGRATION_9_10,
                        MIGRATION_10_11,
                        MIGRATION_11_12,
                        MIGRATION_12_13
                    )
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

        private val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE sessions ADD COLUMN rating INTEGER")
            }
        }

        private val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE sessions ADD COLUMN intervalsEnabled INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE sessions ADD COLUMN warmupEnabled INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE sessions ADD COLUMN caloriesEstimate INTEGER")
                database.execSQL("ALTER TABLE sessions ADD COLUMN durationMinutes INTEGER")
            }
        }

        private val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS achievements (
                        id TEXT NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        type TEXT NOT NULL,
                        threshold INTEGER NOT NULL,
                        progress INTEGER NOT NULL,
                        unlocked INTEGER NOT NULL,
                        unlockedAt INTEGER,
                        icon TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS daily_statuses (
                        date TEXT NOT NULL PRIMARY KEY,
                        status TEXT NOT NULL,
                        sessionCount INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS generated_workout_plans (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        totalDurationMinutes INTEGER NOT NULL,
                        goal TEXT NOT NULL,
                        timeMinutes INTEGER NOT NULL,
                        equipment TEXT NOT NULL,
                        focus TEXT,
                        style TEXT,
                        planJson TEXT NOT NULL,
                        exerciseSignature TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        isSaved INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_9_10 = object : androidx.room.migration.Migration(9, 10) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS progress_photos (
                        photoId TEXT NOT NULL PRIMARY KEY,
                        uid TEXT NOT NULL,
                        poseTag TEXT NOT NULL,
                        notes TEXT,
                        takenAt INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        localPath TEXT,
                        storagePath TEXT NOT NULL,
                        downloadUrl TEXT,
                        thumbUrl TEXT,
                        width INTEGER,
                        height INTEGER,
                        syncStatus TEXT NOT NULL,
                        syncAttempts INTEGER NOT NULL,
                        lastSyncAt INTEGER
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS progress_collages (
                        collageId TEXT NOT NULL PRIMARY KEY,
                        uid TEXT NOT NULL,
                        photoIds TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        localPath TEXT,
                        storagePath TEXT NOT NULL,
                        downloadUrl TEXT,
                        syncStatus TEXT NOT NULL,
                        syncAttempts INTEGER NOT NULL,
                        lastSyncAt INTEGER
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_10_11 = object : androidx.room.migration.Migration(10, 11) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS quick_session_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        dateTime INTEGER NOT NULL,
                        templateId TEXT NOT NULL,
                        durationMinutes INTEGER NOT NULL,
                        completed INTEGER NOT NULL,
                        notes TEXT
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_11_12 = object : androidx.room.migration.Migration(11, 12) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS nutrition_entries (
                        id TEXT NOT NULL,
                        date TEXT NOT NULL,
                        mealType TEXT NOT NULL,
                        name TEXT NOT NULL,
                        calories INTEGER,
                        proteinGrams INTEGER,
                        notes TEXT,
                        createdAt INTEGER NOT NULL,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS pullup_sessions (
                        id TEXT NOT NULL,
                        dateTime INTEGER NOT NULL,
                        repsBySet TEXT NOT NULL,
                        totalReps INTEGER NOT NULL,
                        addedWeightKg REAL,
                        restSeconds INTEGER,
                        notes TEXT,
                        volumeScore INTEGER NOT NULL,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS pullup_max_tests (
                        id TEXT NOT NULL,
                        dateTime INTEGER NOT NULL,
                        maxReps INTEGER NOT NULL,
                        formRating INTEGER,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_12_13 = object : androidx.room.migration.Migration(12, 13) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS workout_sessions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        sessionId TEXT NOT NULL,
                        userId TEXT NOT NULL,
                        planId INTEGER NOT NULL,
                        exercisesJson TEXT NOT NULL,
                        startedAt INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        currentExerciseIndex INTEGER NOT NULL,
                        totalElapsedSeconds INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
