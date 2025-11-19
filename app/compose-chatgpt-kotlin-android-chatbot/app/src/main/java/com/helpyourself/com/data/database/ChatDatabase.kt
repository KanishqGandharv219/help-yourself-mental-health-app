package com.helpyourself.com.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.helpyourself.com.data.database.dao.MessageDao
import com.helpyourself.com.data.database.entity.Conversation
import com.helpyourself.com.data.database.entity.Message
import com.helpyourself.com.data.database.entity.User
import com.helpyourself.com.data.local.entities.ChatSessionEntity
import com.helpyourself.com.data.local.entities.MessageEntity

@Database(
    entities = [
        Message::class,
        User::class,
        Conversation::class,
        ChatSessionEntity::class,
        MessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao

    companion object {
        const val DATABASE_NAME = "chat_database"
    }
}
