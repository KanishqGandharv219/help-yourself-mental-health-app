package com.helpyourself.com.di

import android.content.Context
import com.helpyourself.com.data.local.dao.ChatSessionDao
import com.helpyourself.com.data.local.dao.MessageDao
import com.helpyourself.com.data.local.dao.ChatHistoryDao
import com.helpyourself.com.data.local.dao.DepressionResultDao
import com.helpyourself.com.data.local.dao.AnxietyResultDao
import com.helpyourself.com.data.local.dao.StressResultDao
import com.helpyourself.com.data.local.dao.DepressionAnswerDao
import com.helpyourself.com.data.local.dao.AnxietyAnswerDao
import com.helpyourself.com.data.local.dao.StressAnswerDao
import com.helpyourself.com.data.local.database.AppDatabase
import com.helpyourself.com.data.repository.MentalHealthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }
    
    @Provides
    @Singleton
    fun provideMentalHealthRepository(): MentalHealthRepository {
        return MentalHealthRepository()
    }
    
    @Provides
    fun provideChatSessionDao(database: AppDatabase): ChatSessionDao {
        return database.chatSessionDao()
    }
    
    @Provides
    fun provideMessageDao(database: AppDatabase): MessageDao {
        return database.messageDao()
    }
    
    @Provides
    fun provideChatHistoryDao(database: AppDatabase): ChatHistoryDao {
        return database.chatHistoryDao()
    }

    @Provides
    fun provideDepressionResultDao(database: AppDatabase): DepressionResultDao {
        return database.depressionResultDao()
    }

    @Provides
    fun provideAnxietyResultDao(database: AppDatabase): AnxietyResultDao {
        return database.anxietyResultDao()
    }

    @Provides
    fun provideStressResultDao(database: AppDatabase): StressResultDao {
        return database.stressResultDao()
    }

    @Provides
    fun provideDepressionAnswerDao(database: AppDatabase): DepressionAnswerDao {
        return database.depressionAnswerDao()
    }

    @Provides
    fun provideAnxietyAnswerDao(database: AppDatabase): AnxietyAnswerDao {
        return database.anxietyAnswerDao()
    }

    @Provides
    fun provideStressAnswerDao(database: AppDatabase): StressAnswerDao {
        return database.stressAnswerDao()
    }

}
