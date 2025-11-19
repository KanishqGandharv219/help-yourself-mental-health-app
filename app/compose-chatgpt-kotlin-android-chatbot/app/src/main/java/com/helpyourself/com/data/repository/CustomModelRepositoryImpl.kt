package com.helpyourself.com.data.repository

import android.content.Context
import android.util.Log
import com.helpyourself.com.data.api.ChatRequest
import com.helpyourself.com.data.api.CustomModelService
import com.helpyourself.com.models.ChatType
import com.helpyourself.com.utils.DeviceUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

interface CustomModelRepository {
    suspend fun sendMessage(message: String, chatType: String?, sessionId: String? = null): String
    
    fun streamMessage(message: String, chatType: String?, sessionId: String? = null): Flow<String>
}

@Singleton
class CustomModelRepositoryImpl @Inject constructor(
    private val customModelService: CustomModelService,
    private val okHttpClient: OkHttpClient,
    private val context: Context
) : CustomModelRepository {
    
    private val TAG = "CustomModelRepository"
    private val BASE_URL = "http://192.168.29.103:5002/"
    
    // Define specialized system prompts for different chat types
    private val generalPrompt = "You are a helpful mental health assistant that provides general information and guidance. Respond to the user in a friendly, supportive manner."
    
    private val crisisSupportPrompt = "You are a crisis support assistant. The person you're talking to may be experiencing distress or a mental health crisis. Be supportive, empathetic, and focus on safety. Acknowledge their feelings, provide immediate coping strategies, and suggest professional resources when appropriate. Always prioritize their safety and well-being. If they express thoughts of harm to themselves or others, gently encourage them to seek immediate professional help and provide crisis line information."
    
    private val therapyPrompt = "You are a therapy assistant providing supportive conversation using evidence-based therapeutic approaches. Use techniques like cognitive reframing, validation, open-ended questions, and reflective listening. Help the user explore their thoughts and feelings, but make it clear you're not a replacement for a licensed therapist. Encourage healthy coping skills and self-reflection. Focus on being non-judgmental and supportive."
    
    // Get the appropriate system prompt based on chat type
    private fun getSystemPromptForChatType(chatType: String?): String {
        return when (chatType?.uppercase()) {
            "CRISIS_SUPPORT" -> crisisSupportPrompt
            "THERAPY" -> therapyPrompt
            else -> generalPrompt // Default to general prompt
        }
    }
    
    // Check if this is likely a first message (empty or welcome)
    private fun isWelcomeMessage(message: String): Boolean {
        val result = message.isBlank() || message.trim().lowercase() == "welcome"
        if (result) {
            Log.d(TAG, "Detected welcome message: '$message'")
        }
        return result
    }
    
    // Create appropriate welcome messages for different chat types
    private fun getWelcomeMessageForChatType(chatType: String?): String {
        return when (chatType?.uppercase()) {
            "CRISIS_SUPPORT" -> "I understand you've selected crisis support. I'm here to help during difficult moments. While I'm not a replacement for professional help in emergencies, I can listen and provide support. How are you feeling right now, and how can I help you today?"
            "THERAPY" -> "Welcome to your therapy session space. I'm here to provide a supportive conversation using evidence-based approaches. Remember, I'm not a replacement for a licensed therapist but can help you explore thoughts and feelings. What brings you to therapy today?"
            else -> "Hello! I'm your mental health assistant. I'm here to provide general support and information about mental health topics. How can I help you today?"
        }
    }
    
    override suspend fun sendMessage(message: String, chatType: String?, sessionId: String?): String {
        try {
            // Get device ID for user identification
            val userId = DeviceUtils.getDeviceId(context)
            
            Log.d(TAG, "Sending message: '$message', type: $chatType, session: $sessionId, user: $userId")
            
            // Handle welcome/first message case differently
            if (isWelcomeMessage(message)) {
                Log.d(TAG, "Processing as welcome message for chat type: $chatType")
                val welcomeMessage = getWelcomeMessageForChatType(chatType)
                Log.d(TAG, "Returning welcome message: ${welcomeMessage.take(50)}...")
                return welcomeMessage
            }
            
            // Add the specialized system prompt to the context
            val systemPrompt = getSystemPromptForChatType(chatType)
            val context = mapOf("system_prompt" to systemPrompt)
            
            val request = ChatRequest(
                message = message,
                chat_type = chatType,
                session_id = sessionId,
                user_id = userId,
                context = context
            )
            
            val response = customModelService.sendMessage(request)
            
            Log.d(TAG, "Received response: ${response.response.take(100)}...")
            
            return response.response
        } catch (e: HttpException) {
            // Handle HTTP errors (4xx, 5xx)
            val errorCode = e.code()
            val errorBody = e.response()?.errorBody()?.string()
            Log.e(TAG, "HTTP Error $errorCode: $errorBody", e)
            
            return when (errorCode) {
                404 -> "Error: Server endpoint not found. Please check server configuration."
                500 -> "Error: Server error occurred. The server may be experiencing issues."
                else -> "Error connecting to server: ${e.message() ?: "Unknown HTTP error"}"
            }
        } catch (e: IOException) {
            // Handle network errors
            Log.e(TAG, "Network Error: ${e.message}", e)
            return "Network error: Could not connect to server. Please check your internet connection and server status."
        } catch (e: Exception) {
            // Handle all other errors
            Log.e(TAG, "Unexpected Error: ${e.message}", e)
            return "Error: ${e.message ?: "Unknown error occurred"}"
        }
    }
    
    // Implementation for streaming using OkHttp directly
    // Since Retrofit's streaming with Flow is complex to set up with existing server
    override fun streamMessage(message: String, chatType: String?, sessionId: String?): Flow<String> = flow {
        try {
            // For welcome messages, we'll just emit the welcome message as a single item
            if (isWelcomeMessage(message)) {
                val welcomeMessage = getWelcomeMessageForChatType(chatType)
                emit(welcomeMessage)
                return@flow
            }
            
            // Get device ID for user identification
            val userId = DeviceUtils.getDeviceId(context)
            
            // Add the specialized system prompt to the context
            val systemPrompt = getSystemPromptForChatType(chatType)
            val contextMap = mapOf("system_prompt" to systemPrompt)
            
            // Create JSON request manually
            val jsonRequest = JSONObject().apply {
                put("message", message)
                put("chat_type", chatType ?: "GENERAL")
                put("session_id", sessionId ?: "default-session")
                put("user_id", userId)
                put("stream", true) // Request streaming response
                
                // Add context as a nested object
                val contextObj = JSONObject()
                contextObj.put("system_prompt", systemPrompt)
                put("context", contextObj)
            }
            
            val requestBody = jsonRequest.toString()
                .toRequestBody("application/json".toMediaTypeOrNull())
            
            // Build the request
            val request = Request.Builder()
                .url("${BASE_URL}chat/stream")
                .post(requestBody)
                .build()
            
            // Execute the request
            val response = suspendCoroutine<String> { continuation ->
                okHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        continuation.resumeWithException(
                            IOException("Unexpected response code: ${response.code}")
                        )
                        return@use
                    }
                    
                    // For now, we'll just get the full response
                    // In a real streaming implementation, we would read the stream
                    val responseBody = response.body?.string() ?: ""
                    continuation.resume(responseBody)
                }
            }
            
            // Parse the JSON response
            val jsonResponse = JSONObject(response)
            val responseText = jsonResponse.optString("response", "No response received")
            
            // Simulate streaming by emitting chunks of the text
            // In a real implementation, we would read from a server-sent events stream
            val words = responseText.split(" ")
            var accumulated = ""
            
            for (word in words) {
                accumulated += "$word "
                emit(accumulated.trim())
                kotlinx.coroutines.delay(50) // Simulate streaming delay
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in streamMessage", e)
            emit("Error: ${e.message ?: "Unknown error occurred"}")
        }
    }
} 