package com.helpyourself.com.models

data class MessageModel(
    val id: String = "",
    val content: String = "",
    val role: Role = Role.USER,
    val createdAt: Long = 0
) 