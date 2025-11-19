package com.helpyourself.com.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_history")
data class ChatHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val senderId: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isUser: Boolean
)
