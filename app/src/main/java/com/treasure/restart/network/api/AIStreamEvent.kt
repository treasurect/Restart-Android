package com.treasure.restart.network.api

data class AIStreamEvent(
    var event: String?=null,
    val type: String? = null,
    val content: String = "",
    val conversationId: Long? = null,
    val messageId: Long? = null
)