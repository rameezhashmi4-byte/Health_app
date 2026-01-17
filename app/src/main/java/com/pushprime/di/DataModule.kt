package com.pushprime.di

import android.content.Context
import com.pushprime.data.AuthRepository
import com.pushprime.data.AiCoachSecureStore
import com.pushprime.data.AiCoachSettingsRepository
import com.pushprime.data.DailyStatusDao
import com.pushprime.data.LocalStore
import com.pushprime.data.NutritionPreferencesRepository
import com.pushprime.data.NutritionRepository
import com.pushprime.data.ProfileRepository
import com.pushprime.data.ProgressPhotoRepository
import com.pushprime.data.PullupMaxTestDao
import com.pushprime.data.PullupSessionDao
import com.pushprime.data.StreakRepository
import com.pushprime.data.StepsRepository
import com.pushprime.data.WorkoutGenerator
import com.pushprime.data.WorkoutPlanDao
import com.pushprime.data.WorkoutPlanRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideLocalStore(@ApplicationContext context: Context): LocalStore {
        return LocalStore(context)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository {
        return AuthRepository()
    }

    @Provides
    @Singleton
    fun provideProfileRepository(localStore: LocalStore): ProfileRepository {
        return ProfileRepository(localStore)
    }

    @Provides
    @Singleton
    fun provideStepsRepository(@ApplicationContext context: Context): StepsRepository {
        return StepsRepository(context)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): com.pushprime.data.AppDatabase {
        return com.pushprime.data.AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideSessionDao(database: com.pushprime.data.AppDatabase): com.pushprime.data.SessionDao {
        return database.sessionDao()
    }

    @Provides
    fun providePullupSessionDao(database: com.pushprime.data.AppDatabase): PullupSessionDao {
        return database.pullupSessionDao()
    }

    @Provides
    fun providePullupMaxTestDao(database: com.pushprime.data.AppDatabase): PullupMaxTestDao {
        return database.pullupMaxTestDao()
    }

    @Provides
    @Singleton
    fun provideNutritionRepository(@ApplicationContext context: Context): NutritionRepository {
        return NutritionRepository(context)
    }

    @Provides
    @Singleton
    fun provideNutritionPreferencesRepository(@ApplicationContext context: Context): NutritionPreferencesRepository {
        return NutritionPreferencesRepository(context)
    }

    @Provides
    @Singleton
    fun provideAiCoachSettingsRepository(@ApplicationContext context: Context): AiCoachSettingsRepository {
        return AiCoachSettingsRepository(context)
    }

    @Provides
    @Singleton
    fun provideAiCoachSecureStore(@ApplicationContext context: Context): AiCoachSecureStore {
        return AiCoachSecureStore(context)
    }

    @Provides
    fun provideProgressPhotoDao(database: com.pushprime.data.AppDatabase): com.pushprime.data.ProgressPhotoDao {
        return database.progressPhotoDao()
    }

    @Provides
    fun provideProgressCollageDao(database: com.pushprime.data.AppDatabase): com.pushprime.data.ProgressCollageDao {
        return database.progressCollageDao()
    }

    @Provides
    fun provideDailyStatusDao(database: com.pushprime.data.AppDatabase): DailyStatusDao {
        return database.dailyStatusDao()
    }

    @Provides
    @Singleton
    fun provideStreakRepository(
        @ApplicationContext context: Context,
        sessionDao: com.pushprime.data.SessionDao,
        dailyStatusDao: DailyStatusDao,
        nutritionRepository: NutritionRepository,
        pullupSessionDao: PullupSessionDao,
        pullupMaxTestDao: PullupMaxTestDao
    ): StreakRepository {
        return StreakRepository(
            context = context,
            sessionDao = sessionDao,
            dailyStatusDao = dailyStatusDao,
            nutritionRepository = nutritionRepository,
            pullupSessionDao = pullupSessionDao,
            pullupMaxTestDao = pullupMaxTestDao
        )
    }

    @Provides
    fun provideWorkoutPlanDao(database: com.pushprime.data.AppDatabase): WorkoutPlanDao {
        return database.workoutPlanDao()
    }

    @Provides
    fun provideWorkoutPlanRepository(workoutPlanDao: WorkoutPlanDao): WorkoutPlanRepository {
        return WorkoutPlanRepository(workoutPlanDao)
    }

    @Provides
    fun provideWorkoutGenerator(): WorkoutGenerator {
        return WorkoutGenerator()
    }

    @Provides
    fun provideAchievementDao(database: com.pushprime.data.AppDatabase): com.pushprime.data.AchievementDao {
        return database.achievementDao()
    }

    @Provides
    fun provideQuickSessionDao(database: com.pushprime.data.AppDatabase): com.pushprime.data.QuickSessionDao {
        return database.quickSessionDao()
    }

    @Provides
    @Singleton
    fun provideProgressPhotoRepository(
        @ApplicationContext context: Context,
        photoDao: com.pushprime.data.ProgressPhotoDao,
        collageDao: com.pushprime.data.ProgressCollageDao
    ): ProgressPhotoRepository {
        return ProgressPhotoRepository(context, photoDao, collageDao)
    }
}
