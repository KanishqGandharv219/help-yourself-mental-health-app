package com.helpyourself.com.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val participantId: String,
    val lastMessage: String,
    val timestamp: Long = System.currentTimeMillis()
)
