package com.helpyourself.com.data.database.dao

import androidx.room.*
import com.helpyourself.com.data.database.entity.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {    @Query("SELECT * FROM chat_history_messages WHERE (senderId = :userId AND receiverId = :otherId) OR (senderId = :otherId AND receiverId = :userId) ORDER BY timestamp DESC")
    fun getMessagesForChat(userId: String, otherId: String): Flow<List<Message>>

    @Insert
    suspend fun insertMessage(message: Message)

    @Delete
    suspend fun deleteMessage(message: Message)

    @Query("DELETE FROM chat_history_messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: Long)

    @Query("SELECT * FROM chat_history_messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<Message>>
}
