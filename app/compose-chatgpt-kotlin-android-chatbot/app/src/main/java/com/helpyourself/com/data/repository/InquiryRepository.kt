package com.helpyourself.com.data.repository

import com.helpyourself.com.data.api.ChatRequest
import com.helpyourself.com.data.api.CustomModelService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class InquiryRepository @Inject constructor(
    private val customModelService: CustomModelService
) : CustomModelRepository {
    override suspend fun sendMessage(message: String, chatType: String?, sessionId: String?): String {
        return try {
            val request = ChatRequest(
                message = message,
                chat_type = chatType,
                session_id = sessionId,
                user_id = "android_user"  // Could be personalized in the future
            )
            val response = customModelService.sendMessage(request)
            response.response
        } catch (e: Exception) {
            throw e
        }
    }
    
    // Implement the streamMessage method required by the interface
    override fun streamMessage(message: String, chatType: String?, sessionId: String?): Flow<String> {
        // For inquiry repository, we'll just use the non-streaming version and emit it as a single item
        return flow {
            val response = sendMessage(message, chatType, sessionId)
            emit(response)
        }
    }
} 