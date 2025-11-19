package com.helpyourself.com.models

import java.util.UUID

enum class ChatType {
    GENERAL,        // General conversation
    CRISIS_SUPPORT, // Crisis support chat
    THERAPY         // Therapy session
}

data class ChatSession(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "New Chat",
    val timestamp: Long = System.currentTimeMillis(),
    val isSelected: Boolean = false,
    val type: ChatType = ChatType.GENERAL,
    val lastMessage: String = ""
) 