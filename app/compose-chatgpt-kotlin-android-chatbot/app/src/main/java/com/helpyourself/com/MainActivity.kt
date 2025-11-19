package com.helpyourself.com

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.helpyourself.com.models.ChatType
import com.helpyourself.com.ui.common.AppRoute
import com.helpyourself.com.ui.common.AppScaffold
import com.helpyourself.com.ui.conversations.ConversationViewModel
import com.helpyourself.com.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import android.util.Log

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            val systemInDarkTheme = isSystemInDarkTheme()
            var isDarkMode by remember { mutableStateOf(systemInDarkTheme) }
            var showInitialChatDialog by remember { mutableStateOf(true) }
            
            AppTheme(darkTheme = isDarkMode) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val conversationViewModel = hiltViewModel<ConversationViewModel>()
                    val navController = rememberNavController()
                    
                    // Show initial chat type selection dialog
                    if (showInitialChatDialog) {
                        InitialChatTypeDialog(
                            onDismiss = { 
                                // Close dialog without creating a new chat
                                showInitialChatDialog = false
                                Log.d("MainActivity", "Initial chat dialog dismissed without creating a new chat")
                                // Optionally, ensure we are on the conversation screen
                                if (navController.currentDestination?.route != AppRoute.Conversation.route) {
                                    navController.navigate(AppRoute.Conversation.route) {
                                        popUpTo(AppRoute.Conversation.route) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            },
                            onTypeSelected = { type ->
                                showInitialChatDialog = false
                                // Create chat of selected type and navigate to it
                                val newChatId = conversationViewModel.newConversation(type)
                                Log.d("MainActivity", "Created initial chat with ID: $newChatId and type: ${type.name}")
                                // Navigate to the new chat
                                navController.navigate("${AppRoute.Conversation.route}/$newChatId")
                            }
                        )
                    }
                    
                    AppScaffold(
                        conversationViewModel = conversationViewModel,
                        onToggleTheme = { isDarkMode = !isDarkMode },
                        navController = navController
                    )
                }
            }
        }
    }
}

@Composable
private fun InitialChatTypeDialog(
    onDismiss: () -> Unit,
    onTypeSelected: (ChatType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Welcome! Choose Chat Type") },
        text = {
            Column {
                ChatType.values().forEach { type ->
                    TextButton(
                        onClick = { onTypeSelected(type) }
                    ) {
                        Icon(
                            imageVector = when (type) {
                                ChatType.GENERAL -> Icons.Default.Chat
                                ChatType.CRISIS_SUPPORT -> Icons.Default.Help
                                ChatType.THERAPY -> Icons.Default.Psychology
                            },
                            contentDescription = null
                        )
                        Text(type.name.replace("_", " "))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Start with General Chat")
            }
        }
    )
}

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme()
    } else {
        lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
} 