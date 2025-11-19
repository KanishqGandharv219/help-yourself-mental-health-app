package com.helpyourself.com.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.helpyourself.com.data.local.dao.ChatHistoryDao
import com.helpyourself.com.data.local.entities.ChatHistoryEntity

@Database(
    entities = [ChatHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ChatHistoryDatabase : RoomDatabase() {
    abstract fun chatHistoryDao(): ChatHistoryDao

    companion object {
        const val DATABASE_NAME = "chat_history.db"
    }
}
