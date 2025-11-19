package com.helpyourself.com.ui.conversations

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helpyourself.com.data.repository.ChatRepository
import com.helpyourself.com.data.repository.CustomModelRepository
import com.helpyourself.com.models.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val customModelRepository: CustomModelRepository,
    private val chatRepository: ChatRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val TAG = "ConversationViewModel"
    
    // Current conversation ID
    private val _currentConversation = MutableStateFlow<String?>(null)
    val currentConversationState: StateFlow<String?> = _currentConversation.asStateFlow()

    // List of all conversations
    private val _conversations = MutableStateFlow<List<ChatSession>>(emptyList())
    val conversationsState: StateFlow<List<ChatSession>> = _conversations.asStateFlow()

    // Map of conversationId -> messages
    private val _messages = MutableStateFlow<Map<String, List<MessageModel>>>(emptyMap())
    val messagesState: StateFlow<Map<String, List<MessageModel>>> = _messages.asStateFlow()
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // FAB expansion state
    private val _isFabExpanded = MutableStateFlow(false)
    val isFabExpanded: StateFlow<Boolean> = _isFabExpanded.asStateFlow()
    
    // Flag to stop receiving streaming results
    private var stopReceivingResults = false

    /**
     * If the UI requests to open a chat _before_ the Room flow has emitted the
     * list of existing sessions, we temporarily remember the requested id here
     * and complete the selection once the sessions arrive. This prevents the
     * ViewModel from creating duplicate placeholder chats.
     */
    private var pendingChatId: String? = null

    init {
        Log.d(TAG, "Initializing ConversationViewModel")
        
        // Collect chat sessions from repository
        viewModelScope.launch {
            Log.d(TAG, "Setting up flow collection for chat sessions")
            chatRepository.allChatSessions.collect { chatSessions ->
                Log.d(TAG, "Received ${chatSessions.size} chat sessions from repository")
                _conversations.value = chatSessions
                
                // If we have no current conversation but have chats, select the first one
                if (_currentConversation.value == null && chatSessions.isNotEmpty()) {
                    val firstChat = chatSessions.first()
                    Log.d(TAG, "No current conversation selected, selecting first: ${firstChat.id}")
                    selectChat(firstChat.id)
                }

                // If a chat was requested earlier but wasn't available yet, try selecting it now
                pendingChatId?.let { requestedId ->
                    val match = chatSessions.any { it.id == requestedId }
                    if (match) {
                        Log.d(TAG, "Pending chat $requestedId is now available – selecting it")
                        pendingChatId = null
                        selectChat(requestedId)
                    }
                }
            }
        }
    }

    // Initialize conversations (would be used if loading from backend/DB)
    suspend fun initialize() {
        _isLoading.value = true
        
        try {
            Log.d(TAG, "Initializing conversations")
            
            // If we have a current conversation, fetch its messages
            _currentConversation.value?.let { chatId ->
                Log.d(TAG, "Initialize: Current conversation ID: $chatId")
                
                // Start collecting messages for this chat
                viewModelScope.launch {
                    Log.d(TAG, "Setting up message collection for chat: $chatId")
                    chatRepository.getMessagesByChatId(chatId).collect { messages ->
                        Log.d(TAG, "Received ${messages.size} messages for chat: $chatId")
                        _messages.update { map ->
                            map + (chatId to messages)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing conversations", e)
            _error.value = "Error: ${e.message ?: "Unknown error during initialization"}"
        } finally {
            _isLoading.value = false
        }
    }
    
    // Create a brand-new chat
    fun newConversation(type: ChatType = ChatType.GENERAL): String {
        Log.d(TAG, "Creating new conversation of type: ${type.name}")
        
        val newId = UUID.randomUUID().toString()
        val name = when (type) {
            ChatType.GENERAL -> "General Chat"
            ChatType.CRISIS_SUPPORT -> "Crisis Support"
            ChatType.THERAPY -> "Therapy Session"
        }
        
        // Create new chat session
        val newChat = ChatSession(
            id = newId,
            name = name,
            type = type,
            isSelected = true,
            timestamp = System.currentTimeMillis()
        )
        
        // Insert into database
        viewModelScope.launch {
            try {
                Log.d(TAG, "Inserting new chat session into database: $newId")
                val result = chatRepository.insertChatSession(newChat)
                Log.d(TAG, "Chat session inserted with result: $result")
                
                // Set current chat ID
                _currentConversation.value = newId
                
                // Start collecting messages for this chat
                chatRepository.getMessagesByChatId(newId).collect { messages ->
                    Log.d(TAG, "Received ${messages.size} messages for new chat: $newId")
                    _messages.update { map ->
                        map + (newId to messages)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating new conversation", e)
                _error.value = "Error: ${e.message ?: "Unknown error creating conversation"}"
            }
        }
        
        // Return the new ID so it can be used for navigation
        return newId
    }

    // Select an existing chat
    fun selectChat(chatId: String) {
        Log.d(TAG, "Selecting chat with ID: $chatId")
        Log.d(TAG, "Available chats: ${_conversations.value.map { it.id }}")
        Log.d(TAG, "Available message maps: ${_messages.value.keys}")
        Log.d(TAG, "Current conversation ID before selection: ${_currentConversation.value}")
        
        // Find the chat
        val chat = _conversations.value.find { it.id == chatId }
        if (chat != null) {
            // Update selection status in memory
            _conversations.update { list ->
                list.map { 
                    it.copy(isSelected = it.id == chatId)
                }
            }
            
            // Set current chat ID
            _currentConversation.value = chatId
            Log.d(TAG, "Current conversation ID after selection: ${_currentConversation.value}")
            
            // Start collecting messages for this chat if not already
            if (!_messages.value.containsKey(chatId)) {
                Log.d(TAG, "Setting up message collection for chat: $chatId")
                viewModelScope.launch {
                    chatRepository.getMessagesByChatId(chatId).collect { messages ->
                        Log.d(TAG, "Received ${messages.size} messages for chat: $chatId")
                        _messages.update { map ->
                            map + (chatId to messages)
                        }
                    }
                }
            } else {
                // Log the message count for this chat
                val messages = _messages.value[chatId] ?: emptyList()
                Log.d(TAG, "Selected chat with ID: $chatId, type: ${chat.type}, messages: ${messages.size}")
            }
        } else {
            // Chat list may not have arrived yet. Defer selection until we have the sessions.
            Log.w(TAG, "Chat $chatId not found yet – deferring selection until sessions arrive")
            if (chatId.isNotEmpty()) pendingChatId = chatId
        }
    }

    // Delete a chat
    fun deleteChat(chatId: String) {
        Log.d(TAG, "Deleting chat with ID: $chatId")
        
        viewModelScope.launch {
            try {
                // Delete from database
                chatRepository.deleteChatSession(chatId)
                
                // Remove from messages map
                _messages.update { map ->
                    map - chatId
                }
                
                // If we deleted the current, select the first remaining chat
                if (_currentConversation.value == chatId) {
                    val firstChat = _conversations.value.firstOrNull { it.id != chatId }
                    if (firstChat != null) {
                        Log.d(TAG, "Selecting new chat after deletion: ${firstChat.id}")
                        _currentConversation.value = firstChat.id
                    } else {
                        // No chats left, clear current and create a new one
                        Log.d(TAG, "No chats left after deletion, creating new default chat")
                        _currentConversation.value = null
                        newConversation(ChatType.GENERAL)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting chat", e)
                _error.value = "Error: ${e.message ?: "Unknown error deleting chat"}"
            }
        }
    }
    
    // Update chat name
    fun renameChat(chatId: String, newName: String) {
        Log.d(TAG, "Renaming chat $chatId to: $newName")
        
        viewModelScope.launch {
            try {
                // Find the chat
                val chat = _conversations.value.find { it.id == chatId }
                if (chat != null) {
                    // Update with new name
                    val updatedChat = chat.copy(name = newName)
                    chatRepository.updateChatSession(updatedChat)
                } else {
                    Log.e(TAG, "Cannot rename chat $chatId: not found")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error renaming chat", e)
                _error.value = "Error: ${e.message ?: "Unknown error renaming chat"}"
            }
        }
    }
    
    // Update last message
    fun updateLastMessage(chatId: String, message: String) {
        Log.d(TAG, "Updating last message for chat: $chatId")
        
        viewModelScope.launch {
            try {
                chatRepository.updateLastMessage(chatId, message)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating last message", e)
                _error.value = "Error: ${e.message ?: "Unknown error updating last message"}"
            }
        }
    }

    // Send a message in the current chat
    fun sendMessage(message: String) {
        val chatId = _currentConversation.value ?: return
        
        // Find the chat to get its type
        val chat = _conversations.value.find { it.id == chatId }
        val chatType = chat?.type ?: ChatType.GENERAL
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            stopReceivingResults = false
            
            try {
                // If message is empty (welcome message trigger), don't add user message
                // But add a debug log so we know it's a welcome message
                if (message.isEmpty()) {
                    Log.d(TAG, "Empty message - treating as welcome message trigger for chat $chatId of type $chatType")
                } else if (message.isNotEmpty()) {
                    // Create and add user message
                    val userMessage = MessageModel(
                        id = UUID.randomUUID().toString(),
                        content = message,
                        role = Role.USER,
                        createdAt = System.currentTimeMillis()
                    )
                    
                    // Save to database
                    Log.d(TAG, "Saving user message to database for chat: $chatId")
                    chatRepository.insertMessage(userMessage, chatId)
                }
                
                // Add temporary "thinking" message from assistant
                val tempId = UUID.randomUUID().toString()
                val thinkingMessage = MessageModel(
                    id = tempId,
                    content = "Let me think...",
                    role = Role.ASSISTANT,
                    createdAt = System.currentTimeMillis()
                )
                
                // Add thinking message to state - don't save to database as it's temporary
                _messages.update { map ->
                    val currentMessages = map[chatId] ?: emptyList()
                    map + (chatId to currentMessages + thinkingMessage)
                }
                
                // Set FAB expanded for stop generation button
                _isFabExpanded.value = true
                
                // Log the API call for debugging
                Log.d(TAG, "Sending message to API for chat $chatId of type ${chatType.name}")
                
                // Send message to repository and get response
                val response = customModelRepository.sendMessage(
                    message = message,
                    chatType = chatType.name,
                    sessionId = chatId
                )
                
                Log.d(TAG, "Received response from API for chat $chatId")
                
                // Remove temporary thinking message
                _messages.update { map ->
                    val currentMessages = map[chatId] ?: emptyList()
                    val filteredMessages = currentMessages.filterNot { it.id == tempId }
                    map + (chatId to filteredMessages)
                }
                
                // Check if response contains error
                if (response.startsWith("Error:") || response.startsWith("Network error:")) {
                    _error.value = response
                    Log.e(TAG, "Error response from API: $response")
                } else {
                    // Create and add assistant message
                    val assistantMessage = MessageModel(
                        id = UUID.randomUUID().toString(),
                        content = response,
                        role = Role.ASSISTANT,
                        createdAt = System.currentTimeMillis()
                    )
                    
                    // Save to database
                    Log.d(TAG, "Saving assistant response to database for chat: $chatId")
                    chatRepository.insertMessage(assistantMessage, chatId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending message", e)
                _error.value = "Error: ${e.message ?: "Unknown error"}"
            } finally {
                _isLoading.value = false
                _isFabExpanded.value = false
            }
        }
    }
    
    // Stop generating response
    fun stopGenerating() {
        stopReceivingResults = true
        _isFabExpanded.value = false
    }

    // Explicitly send a welcome message for a chat
    fun sendWelcomeMessage(chatId: String? = null) {
        val targetChatId = chatId ?: _currentConversation.value ?: return
        
        // Find the chat to get its type
        val chat = _conversations.value.find { it.id == targetChatId }
        
        if (chat != null) {
            Log.d(TAG, "Explicitly sending welcome message for chat: $targetChatId (type: ${chat.type})")
            
            // Set this as the current conversation if it's not already
            if (_currentConversation.value != targetChatId) {
                selectChat(targetChatId)
            }
            
            // Send an empty message to trigger the welcome flow
            sendMessage("")
        } else {
            Log.e(TAG, "Cannot send welcome message for unknown chat: $targetChatId")
        }
    }

    // Get messages for a specific chat
    fun getMessages(chatId: String): List<MessageModel> {
        return _messages.value[chatId] ?: emptyList()
    }
    
    // Get current chat type
    fun getCurrentChatType(): ChatType {
        val chatId = _currentConversation.value ?: return ChatType.GENERAL
        return _conversations.value.find { it.id == chatId }?.type ?: ChatType.GENERAL
    }
    
    // Set FAB expanded state
    fun setFabExpanded(expanded: Boolean) {
        _isFabExpanded.value = expanded
    }
    
    // Clear error state
    fun clearError() {
        _error.value = null
    }
} 