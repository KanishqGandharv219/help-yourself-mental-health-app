package com.helpyourself.com.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.helpyourself.com.models.MessageModel
import com.helpyourself.com.models.Role
import java.util.UUID

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["chatId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["chatId"])
    ]
)
data class MessageEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val chatId: String,
    val content: String,
    val role: String,
    val createdAt: Long
) {
    companion object {
        fun fromModel(message: MessageModel, chatId: String): MessageEntity {
            return MessageEntity(
                id = message.id,
                chatId = chatId,
                content = message.content,
                role = message.role.name,
                createdAt = message.createdAt
            )
        }
        
        fun toModel(entity: MessageEntity): MessageModel {
            return MessageModel(
                id = entity.id,
                content = entity.content,
                role = Role.valueOf(entity.role),
                createdAt = entity.createdAt
            )
        }
    }
}
