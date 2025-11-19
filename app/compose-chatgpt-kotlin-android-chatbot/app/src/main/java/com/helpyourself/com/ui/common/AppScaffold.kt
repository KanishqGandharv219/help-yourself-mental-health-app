package com.helpyourself.com.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.helpyourself.com.models.ChatType

import com.helpyourself.com.ui.conversations.ConversationViewModel
import kotlinx.coroutines.launch
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    conversationViewModel: ConversationViewModel = hiltViewModel(),
    onToggleTheme: () -> Unit,
    navController: NavHostController = rememberNavController()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showNewChatDialog by remember { mutableStateOf(false) }
    val currentRoute = navController.currentDestination?.route ?: AppRoute.Conversation.route
    val currentChatId by conversationViewModel.currentConversationState.collectAsState()

    if (showNewChatDialog) {
        AlertDialog(
            onDismissRequest = { 
                Log.d("AppScaffold", "Dialog dismissed by clicking outside")
                showNewChatDialog = false
                // Navigate back to the currently selected chat (if any) to ensure it stays visible
                currentChatId?.let { chatId ->
                    navController.navigate("${AppRoute.Conversation.route}/$chatId") {
                        popUpTo(AppRoute.Conversation.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            },
            title = { Text("Choose Chat Type") },
            text = {
                Column {
                    // Only show valid chat types
                    ChatType.values().forEach { type ->
                        TextButton(
                            onClick = {
                                Log.d("AppScaffold", "Creating new chat with type: ${type.name}")
                                
                                // Create new chat in ConversationViewModel and get the ID directly
                                val newChatId = conversationViewModel.newConversation(type)
                                showNewChatDialog = false
                                
                                Log.d("AppScaffold", "New chat created with ID: $newChatId")
                                
                                // Ensure we're on the conversation screen
                                if (currentRoute != AppRoute.Conversation.route) {
                                    // Navigate to conversation screen with chat ID parameter to force refresh
                                    navController.navigate("${AppRoute.Conversation.route}/$newChatId") {
                                        // Pop up to the conversation route to avoid back stack issues
                                        popUpTo(AppRoute.Conversation.route) { inclusive = true }
                                    }
                                }
                            }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    when (type) {
                                        ChatType.GENERAL -> Icons.Default.Chat
                                        ChatType.CRISIS_SUPPORT -> Icons.Default.Help
                                        ChatType.THERAPY -> Icons.Default.Psychology
                                    },
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(type.name.replace("_", " "))
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showNewChatDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                currentRoute = currentRoute,
                onNewChat = {
                    showNewChatDialog = true
                    scope.launch { drawerState.close() }
                },
                onSelectChat = { chatId ->
                    // Use navigation as the single source of truth
                    Log.d("AppScaffold", "Drawer tapped chatId=$chatId, currentRoute=$currentRoute")
                    
                    // Navigate with chat ID to ensure conversation screen refreshes
                    Log.d("AppScaffold", "About to navigate to ${AppRoute.Conversation.route}/$chatId")
                    navController.navigate("${AppRoute.Conversation.route}/$chatId") {
                        // Pop up to the conversation route to avoid back stack issues
                        popUpTo(AppRoute.Conversation.route) { inclusive = true }
                        launchSingleTop = true
                    }
                    Log.d("AppScaffold", "Navigation called, closing drawer")
                    
                    scope.launch { drawerState.close() }
                },
                onRenameChat = { chatId, newName ->
                    conversationViewModel.renameChat(chatId, newName)
                },
                onDeleteChat = { chatId ->
                    conversationViewModel.deleteChat(chatId)
                },
                navigateToRoute = { route ->
                    navController.navigate(route) {
                        // Simple navigation without using private APIs
                        launchSingleTop = true
                    }
                    scope.launch { drawerState.close() }
                },
                closeDrawer = { scope.launch { drawerState.close() } },
                onToggleTheme = onToggleTheme
            )
        }
    ) {
        AppNavigation(
            navController = navController,
            openDrawer = { scope.launch { drawerState.open() } },
            onToggleTheme = onToggleTheme
        )
    }
} 
