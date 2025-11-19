package com.helpyourself.com.di

import android.content.Context
import androidx.room.Room
import com.helpyourself.com.data.database.ChatHistoryDatabase
import com.helpyourself.com.data.database.dao.MessageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChatHistoryModule {
    
    @Provides
    @Singleton
    @Named("chatHistoryDb")
    fun provideChatHistoryDatabase(
        @ApplicationContext context: Context
    ): ChatHistoryDatabase = Room.databaseBuilder(
        context,
        ChatHistoryDatabase::class.java,
        ChatHistoryDatabase.DATABASE_NAME
    ).build()

    @Provides
    @Named("chatHistoryMessageDao")
    fun provideMessageDao(@Named("chatHistoryDb") database: ChatHistoryDatabase): MessageDao = 
        database.messageDao()
}
