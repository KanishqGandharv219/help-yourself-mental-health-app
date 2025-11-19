package com.helpyourself.com.data.local.dao

import androidx.room.*
import com.helpyourself.com.data.local.entities.ChatSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatSessionDao {
    @Query("SELECT * FROM chat_sessions ORDER BY timestamp DESC")
    fun getAllChatSessions(): Flow<List<ChatSessionEntity>>
    
    @Query("SELECT * FROM chat_sessions WHERE id = :chatId")
    suspend fun getChatSessionById(chatId: String): ChatSessionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chatSession: ChatSessionEntity): Long
    
    @Update
    suspend fun update(chatSession: ChatSessionEntity)
    
    @Delete
    suspend fun delete(chatSession: ChatSessionEntity)
    
    @Query("DELETE FROM chat_sessions WHERE id = :chatId")
    suspend fun deleteByChatId(chatId: String)
    
    @Query("UPDATE chat_sessions SET lastMessage = :message, timestamp = :timestamp WHERE id = :chatId")
    suspend fun updateLastMessage(chatId: String, message: String, timestamp: Long)
}
