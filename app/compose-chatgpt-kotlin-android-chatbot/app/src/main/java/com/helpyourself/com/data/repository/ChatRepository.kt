package com.helpyourself.com.data.repository

import android.util.Log
import com.helpyourself.com.data.local.dao.ChatSessionDao
import com.helpyourself.com.data.local.dao.MessageDao
import com.helpyourself.com.data.local.entities.ChatSessionEntity
import com.helpyourself.com.data.local.entities.MessageEntity
import com.helpyourself.com.models.ChatSession
import com.helpyourself.com.models.MessageModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val chatSessionDao: ChatSessionDao,
    private val messageDao: MessageDao
) {
    private val TAG = "ChatRepository"
    
    // Chat Sessions
    val allChatSessions: Flow<List<ChatSession>> = chatSessionDao.getAllChatSessions().map { entities ->
        Log.d(TAG, "Fetched ${entities.size} chat sessions from database")
        entities.map { ChatSessionEntity.toModel(it) }
    }
    
    suspend fun getChatSessionById(chatId: String): ChatSession? {
        Log.d(TAG, "Getting chat session with ID: $chatId")
        return chatSessionDao.getChatSessionById(chatId)?.let { 
            Log.d(TAG, "Found chat session: ${it.name}")
            ChatSessionEntity.toModel(it) 
        } ?: run {
            Log.d(TAG, "Chat session not found with ID: $chatId")
            null
        }
    }
    
    suspend fun insertChatSession(chatSession: ChatSession): Long {
        Log.d(TAG, "Inserting new chat session: ${chatSession.id}, name: ${chatSession.name}, type: ${chatSession.type}")
        val result = chatSessionDao.insert(ChatSessionEntity.fromModel(chatSession))
        Log.d(TAG, "Chat session inserted with result: $result")
        return result
    }
    
    suspend fun updateChatSession(chatSession: ChatSession) {
        Log.d(TAG, "Updating chat session: ${chatSession.id}, name: ${chatSession.name}")
        chatSessionDao.update(ChatSessionEntity.fromModel(chatSession))
        Log.d(TAG, "Chat session updated successfully")
    }
    
    suspend fun deleteChatSession(chatId: String) {
        Log.d(TAG, "Deleting chat session with ID: $chatId")
        chatSessionDao.deleteByChatId(chatId)
        // Delete all associated messages too
        messageDao.deleteAllByChatId(chatId)
        Log.d(TAG, "Chat session and all associated messages deleted")
    }
    
    suspend fun updateLastMessage(chatId: String, message: String) {
        Log.d(TAG, "Updating last message for chat: $chatId")
        chatSessionDao.updateLastMessage(chatId, message, System.currentTimeMillis())
        Log.d(TAG, "Last message updated")
    }
    
    // Messages
    fun getMessagesByChatId(chatId: String): Flow<List<MessageModel>> {
        Log.d(TAG, "Getting messages flow for chat ID: $chatId")
        return messageDao.getMessagesByChatId(chatId).map { entities ->
            Log.d(TAG, "Fetched ${entities.size} messages for chat: $chatId")
            entities.map { MessageEntity.toModel(it) }
        }
    }
    
    suspend fun insertMessage(message: MessageModel, chatId: String): Long {
        Log.d(TAG, "Inserting message: ${message.id} for chat: $chatId, role: ${message.role}")
        val result = messageDao.insert(MessageEntity.fromModel(message, chatId))
        // Also update the last message in the chat session
        updateLastMessage(chatId, message.content)
        Log.d(TAG, "Message inserted with result: $result")
        return result
    }
    
    suspend fun insertMessages(messages: List<MessageModel>, chatId: String) {
        Log.d(TAG, "Inserting ${messages.size} messages for chat: $chatId")
        val entities = messages.map { MessageEntity.fromModel(it, chatId) }
        messageDao.insertAll(entities)
        
        // Update last message if there are messages
        if (messages.isNotEmpty()) {
            Log.d(TAG, "Updating last message from batch insert")
            updateLastMessage(chatId, messages.last().content)
        }
        Log.d(TAG, "Batch message insert completed")
    }
    
    suspend fun deleteMessage(messageId: String) {
        Log.d(TAG, "Deleting message with ID: $messageId")
        messageDao.deleteById(messageId)
        Log.d(TAG, "Message deleted")
    }
}
