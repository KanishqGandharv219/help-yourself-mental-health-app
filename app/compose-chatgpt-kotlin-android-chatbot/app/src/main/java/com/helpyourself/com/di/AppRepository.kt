package com.helpyourself.com.di

import android.content.Context
import com.helpyourself.com.data.repository.CustomModelRepository
import com.helpyourself.com.data.repository.CustomModelRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.helpyourself.com.data.api.CustomModelService
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindCustomModelRepository(
        repo: CustomModelRepositoryImpl
    ): CustomModelRepository
    
    companion object {
        @Provides
        @Singleton
        fun provideCustomModelRepositoryImpl(
            customModelService: CustomModelService,
            okHttpClient: OkHttpClient,
            @ApplicationContext context: Context
        ): CustomModelRepositoryImpl {
            return CustomModelRepositoryImpl(customModelService, okHttpClient, context)
        }
    }
} 