package com.treasure.restart.network.api

import com.treasure.basic.network.BaseResponse
import com.treasure.restart.bean.AIChatConversation
import com.treasure.restart.bean.AIChatMessage
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query
import retrofit2.http.Streaming

interface AIChatApi {

    @POST("api/ai/chat/guest")
    suspend fun sendMessageAsGuest(@Body body: RequestBody): BaseResponse<AIChatResponse>

    @POST("api/ai/chat/user")
    suspend fun sendMessage(@Body body: RequestBody): BaseResponse<AIChatResponse>

    /**
     * AI 流式聊天
     */
    @Streaming
    @POST("api/ai/chat/stream")
    suspend fun sendMessageStream(@Body body: RequestBody): ResponseBody

    /**
     * 获取当前用户的会话列表
     */
    @GET("api/ai/conversation/list")
    suspend fun loadCurConversations(): BaseResponse<List<AIChatConversation>>

    /**
     * 根据会话 ID 获取消息
     */
    @GET("api/ai/message/list")
    suspend fun loadMessagesByCid(
        @Query("conversationId") conversationId: Long
    ): BaseResponse<List<AIChatMessage>>

    @PUT("api/ai/conversation/title")
    suspend fun renameConversation(
        @Query("conversationId") conversationId: Long, @Query("title") title: String
    ): BaseResponse<Void>

    @DELETE("api/ai/conversation")
    suspend fun deleteConversation(
        @Query("conversationId") conversationId: Long
    ): BaseResponse<Void>
}

data class AIChatResponse(
    val reply: String, val conversationId: Long?
)
