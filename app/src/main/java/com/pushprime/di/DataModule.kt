package com.pushprime.di

import android.content.Context
import com.pushprime.data.AuthRepository
import com.pushprime.data.LocalStore
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
}
