package com.treasure.restart.network.repository

import com.treasure.basic.network.RetrofitClient
import com.treasure.basic.network.safeApiCall
import com.treasure.basic.utils.JsonUtils
import com.treasure.restart.network.api.AIChatApi
import com.treasure.restart.network.api.AIStreamEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class AIChatRepository {

    private val api = RetrofitClient.getInstance().create(AIChatApi::class.java)

    fun loadCurConversations() = safeApiCall {
        api.loadCurConversations()
    }

    fun loadMessagesByCid(conversationId: Long) = safeApiCall {
        api.loadMessagesByCid(conversationId)
    }

    fun sendMessage(conversationId: Long?, message: String) = safeApiCall {
        val requestBody = JsonUtils.toJson(
            mapOf<String, Any?>(
                "message" to message, "conversationId" to conversationId
            )
        ).toRequestBody("application/json; charset=utf-8".toMediaType())

        api.sendMessage(requestBody)
    }

    fun sendMessageStream(conversationId: Long?, message: String): Flow<AIStreamEvent> = flow {

        val requestBody = JsonUtils.toJson(
            mapOf<String, Any?>("message" to message, "conversationId" to conversationId)
        ).toRequestBody("application/json; charset=utf-8".toMediaType())

        val responseBody = api.sendMessageStream(requestBody)
        try {
            responseBody.byteStream().bufferedReader(Charsets.UTF_8).use { reader ->
                var currentEvent = ""

                while (true) {

                    val line = reader.readLine() ?: break

                    // 空行表示一个 SSE Event 结束
                    if (line.isBlank()) {
                        currentEvent = ""
                        continue
                    }

                    // event:token
                    if (line.startsWith("event:")) {
                        currentEvent = line.substringAfter("event:").trim()
                        continue
                    }

                    // data:xxxx
                    if (line.startsWith("data:")) {
                        val data = line.substringAfter("data:").trim()
                        if (data.isEmpty()) continue
                        try {

                            val eventData = JsonUtils.fromJson(data, AIStreamEvent::class.java)
                            eventData.event = currentEvent
                            emit(eventData)
                        } catch (e: Exception) {
                            emit(
                                AIStreamEvent(
                                    event = "error", content = "解析 SSE 数据失败：${e.message}"
                                )
                            )
                        }
                    }
                }
            }
        } finally {
            responseBody.close()
        }

    }.flowOn(Dispatchers.IO)

    fun updateConversationTitle(conversationId: Long, title: String) = safeApiCall {
        api.renameConversation(conversationId, title)
    }

    fun deleteConversation(conversationId: Long) = safeApiCall {
        api.deleteConversation(conversationId)
    }
}
