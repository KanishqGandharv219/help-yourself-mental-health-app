package com.helpyourself.com.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.helpyourself.com.models.ChatSession
import com.helpyourself.com.models.ChatType
import java.util.UUID

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val timestamp: Long,
    val type: String,
    val lastMessage: String = ""
) {
    companion object {
        fun fromModel(chatSession: ChatSession): ChatSessionEntity {
            return ChatSessionEntity(
                id = chatSession.id,
                name = chatSession.name,
                timestamp = chatSession.timestamp,
                type = chatSession.type.name,
                lastMessage = chatSession.lastMessage
            )
        }
        
        fun toModel(entity: ChatSessionEntity, isSelected: Boolean = false): ChatSession {
            return ChatSession(
                id = entity.id,
                name = entity.name,
                timestamp = entity.timestamp,
                type = ChatType.valueOf(entity.type),
                isSelected = isSelected,
                lastMessage = entity.lastMessage
            )
        }
    }
} 
