package com.helpyourself.com.di

import android.content.Context
import com.helpyourself.com.data.repository.InquiryRepository
import com.helpyourself.com.data.api.CustomModelService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideInquiryRepository(customModelService: CustomModelService): InquiryRepository {
        return InquiryRepository(customModelService)
    }
} 