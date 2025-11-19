package com.helpyourself.com.ui.conversations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helpyourself.com.models.ChatType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

/**
 * This ViewModel manages chat sessions selection state
 */
@HiltViewModel
class ChatSessionsViewModel @Inject constructor() : ViewModel() {
    private val TAG = "ChatSessionsViewModel"

    // Selected chat ID
    private val _selectedChatId = MutableStateFlow<String?>(null)
    val selectedChatId: StateFlow<String?> = _selectedChatId.asStateFlow()

    // Create a new chat (handled by UI coordination with ConversationViewModel)
    fun createNewChat(type: ChatType = ChatType.GENERAL, sendWelcome: Boolean = true) {
        Log.d(TAG, "Creating new chat of type: ${type.name}")
        // UI will coordinate with ConversationViewModel
    }

    // Select a chat 
    fun selectChat(chatId: String) {
        Log.d(TAG, "Selecting chat ID: $chatId")
        _selectedChatId.value = chatId
    }

    // Rename a chat (handled by UI coordination with ConversationViewModel)
    fun renameChat(chatId: String, newName: String) {
        Log.d(TAG, "Renaming chat: $chatId to $newName")
        // UI will coordinate with ConversationViewModel
    }

    // Delete a chat (handled by UI coordination with ConversationViewModel)
    fun deleteChat(chatId: String) {
        Log.d(TAG, "Deleting chat: $chatId")
        // UI will coordinate with ConversationViewModel
    }

    // Update last message (handled by UI coordination with ConversationViewModel)
    fun updateLastMessage(chatId: String, message: String) {
        Log.d(TAG, "Updating last message for chat: $chatId")
        // UI will coordinate with ConversationViewModel
    }
} 