package com.helpyourself.com.data.local.dao

import androidx.room.*
import com.helpyourself.com.data.local.entities.ChatHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatHistoryDao {
    @Query("SELECT * FROM chat_history WHERE senderId = :sessionId ORDER BY timestamp DESC")
    fun getChatHistory(sessionId: String): Flow<List<ChatHistoryEntity>>

    @Insert
    suspend fun insertChat(chat: ChatHistoryEntity)

    @Query("DELETE FROM chat_history WHERE senderId = :sessionId")
    suspend fun clearChatHistory(sessionId: String)
    
    @Query("SELECT * FROM chat_history ORDER BY timestamp DESC")
    fun getAllChats(): Flow<List<ChatHistoryEntity>>
}
