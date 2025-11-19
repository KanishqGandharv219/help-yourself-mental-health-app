package com.helpyourself.com.ui.common

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Support
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.helpyourself.com.R
import com.helpyourself.com.models.ChatSession
import com.helpyourself.com.models.ChatType
import com.helpyourself.com.ui.conversations.ConversationViewModel
import com.helpyourself.com.ui.navigation.Routes
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

private const val TAG = "AppDrawer"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawer(
    currentRoute: String,
    navigateToRoute: (String) -> Unit,
    closeDrawer: () -> Unit,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
    conversationViewModel: ConversationViewModel = hiltViewModel(),
    onNewChat: () -> Unit = {},
    onSelectChat: (String) -> Unit = {},
    onRenameChat: (String, String) -> Unit = { _, _ -> },
    onDeleteChat: (String) -> Unit = {}
) {
    val conversationsState by conversationViewModel.conversationsState.collectAsState()
    val currentChatId by conversationViewModel.currentConversationState.collectAsState()
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    
    Log.d(TAG, "AppDrawer composing with currentRoute: $currentRoute, currentChatId: $currentChatId")
    Log.d(TAG, "Available chats: ${conversationsState.size}")
    
    ModalDrawerSheet(modifier = modifier) {
        // App Logo and Title
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .padding(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Mental Health Assistant",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        
        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(bottom = 8.dp)
        ) {
            // Navigation Section
            Text(
                "Navigation",
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            
            // Chat Item with Add Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavigationDrawerItem(
                    label = { Text("Chat") },
                    selected = currentRoute.startsWith(AppRoute.Conversation.route),
                    onClick = { navigateToRoute(AppRoute.Conversation.route) },
                    icon = { Icon(Icons.Default.Chat, contentDescription = null) },
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(
                    onClick = { 
                        Log.d(TAG, "New chat button clicked")
                        onNewChat() 
                    },
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "New Chat",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Chat Sessions List
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                if (conversationsState.isNotEmpty()) {
                    Text(
                        "Your Chats",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Log.d(TAG, "Rendering ${conversationsState.size} chat sessions")
                    conversationsState.forEach { session ->
                        Log.d(TAG, "Rendering chat session: ${session.id}, name: ${session.name}, type: ${session.type}, selected: ${session.isSelected}")
                        ChatSessionItem(
                            chatSession = session,
                            isSelected = session.id == currentChatId,
                            onSelect = { 
                                Log.d(TAG, "ChatSessionItem onSelect for id=${session.id}")
                                onSelectChat(session.id) 
                            },
                            onRename = { newName -> 
                                Log.d(TAG, "Renaming chat ${session.id} to '$newName'")
                                onRenameChat(session.id, newName) 
                            },
                            onDelete = { 
                                Log.d(TAG, "Deleting chat ${session.id}")
                                onDeleteChat(session.id) 
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    Log.d(TAG, "No chat sessions to display")
                    Text(
                        "No chats yet",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Academic Resources
            NavigationDrawerItem(
                label = { Text("Academic Resources") },
                selected = currentRoute == AppRoute.AcademicResources.route,
                onClick = { navigateToRoute(AppRoute.AcademicResources.route) },
                icon = { Icon(Icons.Default.Book, contentDescription = null) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            
            // Mental Health Resources
            NavigationDrawerItem(
                label = { Text("Mental Health Resources") },
                selected = currentRoute == AppRoute.MentalHealthResources.route,
                onClick = { navigateToRoute(AppRoute.MentalHealthResources.route) },
                icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            
            // Mental Health Analysis
            NavigationDrawerItem(
                label = { Text("Mental Health Analysis") },
                selected = currentRoute == AppRoute.MentalHealthAnalysis.route,
                onClick = { navigateToRoute(AppRoute.MentalHealthAnalysis.route) },
                icon = { Icon(Icons.Default.Psychology, contentDescription = null) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            
            // Database Test
            NavigationDrawerItem(
                label = { Text("Test Database Connection") },
                selected = currentRoute == AppRoute.DatabaseTest.route,
                onClick = { navigateToRoute(AppRoute.DatabaseTest.route) },
                icon = { Icon(Icons.Default.Storage, contentDescription = null) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            
            // Inquiry Section
            NavigationDrawerItem(
                label = { Text("Inquiry & Assessment") },
                selected = currentRoute == AppRoute.Inquiry.route,
                onClick = { navigateToRoute(AppRoute.Inquiry.route) },
                icon = { Icon(Icons.Default.QuestionAnswer, contentDescription = null) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            // Therapy Near Me Section
            NavigationDrawerItem(
                label = { Text("Therapy Near Me") },
                selected = currentRoute == AppRoute.TherapyNearMe.route,
                onClick = { 
                    Log.d(TAG, "Therapy Near Me clicked")
                    scope.launch {
                        navigateToRoute(AppRoute.TherapyNearMe.route)
                        closeDrawer()
                    }
                },
                icon = { Icon(Icons.Default.Psychology, contentDescription = null) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            
            // Settings Section
            Text(
                "Settings",
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            
            // Settings
            NavigationDrawerItem(
                label = { Text("Settings") },
                selected = currentRoute == AppRoute.Settings.route,
                onClick = { navigateToRoute(AppRoute.Settings.route) },
                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            
            // About
            NavigationDrawerItem(
                label = { Text("About") },
                selected = currentRoute == AppRoute.About.route,
                onClick = { navigateToRoute(AppRoute.About.route) },
                icon = { Icon(Icons.Default.Info, contentDescription = null) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
        
        // Theme Toggle (Simple Button) - outside scroll area
        Button(
            onClick = { 
                Log.d(TAG, "Theme toggle button clicked")
                onToggleTheme()
                closeDrawer() 
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.DarkMode, 
                    contentDescription = "Toggle Theme"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Toggle Theme")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatSessionItem(
    chatSession: ChatSession,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    // Rename Dialog
    if (showRenameDialog) {
        var newName by remember { mutableStateOf(chatSession.name) }
        
        Log.d(TAG, "Showing rename dialog for chat: ${chatSession.id}")
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Chat") },
            text = {
                TextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New name") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        Log.d(TAG, "Confirming rename of chat ${chatSession.id} to '$newName'")
                        onRename(newName)
                        showRenameDialog = false
                    }
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    Log.d(TAG, "Cancelling rename dialog for chat ${chatSession.id}")
                    showRenameDialog = false 
                }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Delete Confirmation Dialog
    if (showDeleteConfirmation) {
        Log.d(TAG, "Showing delete confirmation for chat: ${chatSession.id}")
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Chat") },
            text = { Text("Are you sure you want to delete this chat?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        Log.d(TAG, "Confirming deletion of chat ${chatSession.id}")
                        onDelete()
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    Log.d(TAG, "Cancelling delete confirmation for chat ${chatSession.id}")
                    showDeleteConfirmation = false 
                }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = {
            Log.d(TAG, "Card onClick for chat: ${chatSession.id}, name: ${chatSession.name}")
            onSelect()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Chat type icon
            Icon(
                getChatTypeIcon(chatSession.type),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = chatSession.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                
                if (chatSession.lastMessage.isNotEmpty()) {
                    Text(
                        text = chatSession.lastMessage,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = formatTimestamp(chatSession.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Action buttons
            Row {
                IconButton(
                    onClick = { 
                        Log.d(TAG, "Rename button clicked for chat ${chatSession.id}")
                        showRenameDialog = true 
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Rename",
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                IconButton(
                    onClick = { 
                        Log.d(TAG, "Delete button clicked for chat ${chatSession.id}")
                        showDeleteConfirmation = true 
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// Helper functions
@Composable
private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun getChatTypeIcon(type: ChatType): ImageVector {
    return when (type) {
        ChatType.GENERAL -> Icons.Default.Chat
        ChatType.CRISIS_SUPPORT -> Icons.Default.Help
        ChatType.THERAPY -> Icons.Default.Psychology
    }
}