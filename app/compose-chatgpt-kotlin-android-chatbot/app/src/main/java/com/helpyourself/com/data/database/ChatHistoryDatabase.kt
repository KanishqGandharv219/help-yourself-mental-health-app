package com.helpyourself.com.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.helpyourself.com.data.database.dao.MessageDao
import com.helpyourself.com.data.database.entity.Conversation
import com.helpyourself.com.data.database.entity.Message
import com.helpyourself.com.data.database.entity.User

@Database(
    entities = [Message::class, User::class, Conversation::class],
    version = 1,
    exportSchema = false
)
abstract class ChatHistoryDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    
    companion object {
        const val DATABASE_NAME = "chat_history_database"
    }
}
