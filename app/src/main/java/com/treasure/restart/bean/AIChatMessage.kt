package com.treasure.restart.bean

data class AIChatMessage(
    val id: Long = 0L,
    val content: String = "",
    val conversationId: Long = 0L,
    val role: String = "",
    val isUser: Boolean = false
)