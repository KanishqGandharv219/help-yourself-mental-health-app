package com.helpyourself.com.data.api

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Streaming
import okhttp3.ResponseBody

data class ChatRequest(
    val message: String,
    val chat_type: String? = null,
    val session_id: String? = null,
    val user_id: String = "android_user",
    val context: Map<String, Any>? = null,
    val stream: Boolean = false
)

data class ChatResponse(
    val response: String
)

data class StreamChunk(
    val chunk: String,
    val done: Boolean = false
)

interface CustomModelService {
    @POST("chat")
    suspend fun sendMessage(@Body request: ChatRequest): ChatResponse
    
    @Streaming
    @POST("chat/stream")
    suspend fun streamMessage(@Body request: ChatRequest): ResponseBody
}