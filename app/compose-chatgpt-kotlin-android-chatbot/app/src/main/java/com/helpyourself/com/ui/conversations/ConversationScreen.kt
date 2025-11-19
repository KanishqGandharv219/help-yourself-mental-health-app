package com.helpyourself.com.ui.conversations

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.helpyourself.com.models.Role
import com.helpyourself.com.ui.conversations.components.ChatInput
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip

private const val TAG = "ConversationScreen"

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun ConversationScreen(
    chatId: String? = null,
    viewModel: ConversationViewModel = hiltViewModel()
) {
    val messagesMap by viewModel.messagesState.collectAsState()
    val currentChatId by viewModel.currentConversationState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val conversationsState by viewModel.conversationsState.collectAsState()
    val isFabExpanded by viewModel.isFabExpanded.collectAsState()
    
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Use chatId parameter directly from navigation
    val effectiveChatId = chatId ?: currentChatId
    val messages = effectiveChatId?.let { messagesMap[it] } ?: emptyList()
    
    // Debug logging
    Log.d(TAG, "Composing with chatId: $chatId, effectiveChatId: $effectiveChatId")
    Log.d(TAG, "Available chats: ${conversationsState.map { it.id }}")
    Log.d(TAG, "Messages for this chat: ${messages.size}")
    Log.d(TAG, "All message keys: ${messagesMap.keys}")
    
    // Log conversation details
    if (effectiveChatId != null) {
        val selectedChat = conversationsState.find { it.id == effectiveChatId }
        if (selectedChat != null) {
            Log.d(TAG, "Current chat details - ID: ${selectedChat.id}, Name: ${selectedChat.name}, Type: ${selectedChat.type}")
            Log.d(TAG, "Last message: ${selectedChat.lastMessage}, Timestamp: ${selectedChat.timestamp}")
        } else {
            Log.d(TAG, "Current chat ID $effectiveChatId not found in conversation list")
        }
    }
    
    val listState = rememberLazyListState()
    
    // Handle chat selection - only using chatId from navigation
    LaunchedEffect(chatId) {
        if (chatId != null) {
            Log.d(TAG, "LaunchedEffect: Selecting chat ID from navigation: $chatId")
            Log.d(TAG, "Current conversation ID before selection: ${viewModel.currentConversationState.value}")
            viewModel.selectChat(chatId)
            Log.d(TAG, "Current conversation ID after selection: ${viewModel.currentConversationState.value}")
        } else {
            Log.d(TAG, "LaunchedEffect: chatId is null, not selecting any chat")
        }
    }
    
    // Send welcome message if needed - use a separate key that includes messages size
    // to ensure this effect runs only when needed
    LaunchedEffect(effectiveChatId, messages.isEmpty()) {
        if (effectiveChatId != null && messages.isEmpty()) {
            // Find selected chat
            val selectedChat = conversationsState.find { it.id == effectiveChatId }
            selectedChat?.let {
                Log.d(TAG, "Sending welcome message for chat ID: $effectiveChatId, type: ${it.type}")
                // Explicitly send empty message to trigger welcome message with a small delay to ensure state is updated
                kotlinx.coroutines.delay(100)
                viewModel.sendMessage("")
            }
        }
    }
    
    // Auto-scroll to bottom when new message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            Log.d(TAG, "New message detected, scrolling to bottom. Total messages: ${messages.size}")
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Wrap everything in a root Box that dismisses the keyboard
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { 
                    Log.d(TAG, "Tap detected outside TextField - clearing focus")
                    focusManager.clearFocus()
                    keyboardController?.hide() 
                }
            }
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (effectiveChatId == null) {
                    // No chat selected
                    Log.d(TAG, "No chat selected, showing placeholder")
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Select a chat or create a new one")
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                reverseLayout = false,
                                state = listState,
                                contentPadding = WindowInsets.statusBars
                                    .add(WindowInsets(top = 90.dp, bottom = 80.dp))
                                    .asPaddingValues()
                            ) {
                                items(messages) { message ->
                                    // Log message details for debugging
                                    Log.v(TAG, "Displaying message: ${message.id}, role: ${message.role}, content: ${message.content.take(20)}...")
                                    
                                    MessageBubble(
                                        message = message.content,
                                        isUser = message.role == Role.USER
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }

                            // Stop generating FAB
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp)
                            ) {
                                if (isFabExpanded) {
                                    Log.d(TAG, "Showing stop generation button")
                                    FloatingActionButton(
                                        onClick = { 
                                            Log.d(TAG, "Stop generation button clicked")
                                            viewModel.stopGenerating() 
                                        },
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Stop,
                                            contentDescription = "Stop Generating",
                                            tint = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }

                            // Show error message if there is one
                            error?.let { errorMessage ->
                                Log.e(TAG, "Showing error: $errorMessage")
                                Snackbar(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .align(Alignment.BottomCenter),
                                    action = {
                                        TextButton(onClick = { viewModel.clearError() }) {
                                            Text("Dismiss")
                                        }
                                    }
                                ) {
                                    Text(errorMessage)
                                }
                            }
                        }

                        // Chat input at the bottom
                        ChatInput(
                            onSendMessage = { message ->
                                if (message.isNotBlank()) {
                                    Log.d(TAG, "Sending user message: ${message.take(20)}...")
                                    viewModel.sendMessage(message)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Loading indicator shows only when there are no messages yet (e.g., initial load)
                if (isLoading && messages.isEmpty()) {
                    Log.d(TAG, "Loading indicator visible (no messages yet)")
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: String,
    isUser: Boolean
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDarkTheme = isSystemInDarkTheme()
    
    val gradientColors = if (isUser) {
        listOf(
            colorScheme.primary.copy(alpha = 0.8f),
            colorScheme.primaryContainer.copy(alpha = 0.9f)
        )
    } else {
        listOf(
            colorScheme.surfaceVariant.copy(alpha = 0.9f),
            colorScheme.surface.copy(alpha = 0.8f)
        )
    }

    Box(
        modifier = Modifier
            .padding(
                start = if (isUser) 64.dp else 8.dp,
                end = if (isUser) 8.dp else 64.dp,
                top = 4.dp,
                bottom = 4.dp
            )
            .fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .align(if (isUser) Alignment.CenterEnd else Alignment.CenterStart),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            )
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(gradientColors),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = message,
                    color = when {
                        isUser && !isDarkTheme -> Color.Black
                        isUser -> colorScheme.onPrimary
                        else -> colorScheme.onSurface
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.widthIn(max = 280.dp)
                )
            }
        }
    }
} 