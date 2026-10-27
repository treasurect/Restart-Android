package com.treasure.restart.func.main.ai

import android.app.Application
import android.telecom.CallException
import android.text.TextUtils
import androidx.lifecycle.viewModelScope
import com.treasure.basic.base.BaseViewModel
import com.treasure.basic.network.ApiResult
import com.treasure.basic.network.asResult
import com.treasure.restart.bean.AIChatConversation
import com.treasure.restart.bean.AIChatMessage
import com.treasure.restart.network.repository.AIChatRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Collections
import kotlin.time.Duration.Companion.milliseconds

class AIChatViewModel(application: Application) : BaseViewModel(application) {

    private val repository = AIChatRepository()

    private val _messages = MutableStateFlow<List<AIChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _currentConversationId = MutableStateFlow<Long?>(null)
    val currentConversationId: StateFlow<Long?> = _currentConversationId.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private

    fun sendMessage(content: String) {
        val message = content.trim()
        if (message.isEmpty() || _loading.value) return

        _messages.value += AIChatMessage(content = message, isUser = true)
        _loading.value = true
        viewModelScope.launch {
            try {
                repository.sendMessage(_currentConversationId.value, message).asResult()
                    .collectLatest { result ->
                        when (result) {
                            is ApiResult.Success -> {
                                val reply = result.data?.reply?.takeIf { it.isNotBlank() }
                                    ?: "暂时没有获取到有效回答。"
                                this@AIChatViewModel._currentConversationId.value =
                                    result.data?.conversationId
                                _messages.value += AIChatMessage(content = reply, isUser = false)
                            }

                            is ApiResult.Error -> {
                                _messages.value += AIChatMessage(
                                    content = "请求失败：${result.msg}", isUser = false
                                )
                            }
                        }
                    }
            } catch (e: Exception) {
                _messages.value += AIChatMessage(
                    content = "请求失败：${e.message ?: "未知错误"}", isUser = false
                )
            } finally {
                _loading.value = false
            }
        }
    }

    private var isFirstToken = true
    private val _thinking = MutableStateFlow(false)
    val thinking = _thinking.asStateFlow()
    private var streamJob: Job? = null
    private var curMessageId: Long? = null

    /**
     * 流式sse
     */
    fun sendMessageStream(content: String) {
        isFirstToken = true

        val message = content.trim()

        if (message.isEmpty() || _loading.value) {
            return
        }

        // 用户消息
        val userMessage = AIChatMessage(
            id = -System.currentTimeMillis(),
            content = message,
            conversationId = _currentConversationId.value ?: 0L,
            role = "user",
            isUser = true
        )
        _thinking.value = true

        _messages.value += userMessage

        _loading.value = true

        streamJob = viewModelScope.launch {

            var assistantMessageId = -System.currentTimeMillis() - 1
            curMessageId = assistantMessageId
            // 请求接口前，立即显示思考提示
            _messages.value += AIChatMessage(
                id = assistantMessageId,
                content = "AI 正在思考中...",
                conversationId = _currentConversationId.value ?: 0L,
                role = "assistant",
                isUser = false
            )
            try {
                repository.sendMessageStream(_currentConversationId.value, message)
                    .collect { event ->
                        when (event.event) {
                            "start" -> {
                                event.conversationId?.let {
                                    _currentConversationId.value = it
                                }
                            }

                            "token" -> {
                                if (isFirstToken) {
                                    isFirstToken = false

                                    val currentMessages = _messages.value
                                    val index = currentMessages.indexOfFirst {
                                        it.id == assistantMessageId
                                    }

                                    if (index >= 0) {
                                        val newList = currentMessages.toMutableList()
                                        newList[index] = newList[index].copy(content = "")
                                        _messages.value = newList
                                    }
                                }

                                val content = event.content
                                if (content.isEmpty()) return@collect

                                event.conversationId?.let {
                                    _currentConversationId.value = it
                                }

//                            // 按 Unicode 码点拆分，避免拆坏 emoji 等字符
//                            val chars = content.codePoints()
//                                .toArray()
//                                .map { String(Character.toChars(it)) }

//                            for (char in chars) {
                                val currentMessages = _messages.value
                                val index = currentMessages.indexOfFirst {
                                    it.id == assistantMessageId
                                }

                                if (index >= 0) {
                                    val newList = currentMessages.toMutableList()
                                    val oldMessage = newList[index]

                                    newList[index] = oldMessage.copy(
                                        content = oldMessage.content + content
                                    )

                                    _messages.value = newList
                                }

                                delay(10L.milliseconds)
//                            }
                            }

                            "complete" -> {
                                event.conversationId?.let {
                                    _currentConversationId.value = it
                                }

                                event.messageId?.let { serverMessageId ->

                                    val currentMessages = _messages.value

                                    val index = currentMessages.indexOfFirst {
                                        it.id == assistantMessageId
                                    }

                                    if (index >= 0) {

                                        val oldMessage = currentMessages[index]

                                        val newMessage = oldMessage.copy(
                                            id = serverMessageId
                                        )

                                        val newList = currentMessages.toMutableList()

                                        newList[index] = newMessage

                                        _messages.value = newList
                                    }
                                }
                                _thinking.value = false
                            }

                            "error" -> {
                                val currentMessages = _messages.value

                                val index = currentMessages.indexOfFirst {
                                    it.id == assistantMessageId
                                }

                                if (index >= 0) {

                                    val oldMessage = currentMessages[index]

                                    val newMessage = oldMessage.copy(
                                        content = if (event.content.isNotBlank()) {
                                            event.content
                                        } else {
                                            "AI 回复失败"
                                        }
                                    )

                                    val newList = currentMessages.toMutableList()

                                    newList[index] = newMessage

                                    _messages.value = newList

                                } else {

                                    _messages.value += AIChatMessage(
                                        id = assistantMessageId, content = event.content.ifBlank {
                                            "AI 回复失败"
                                        }, role = "assistant", isUser = false
                                    )
                                }
                                _thinking.value = false
                            }
                        }
                    }

            } catch (e: CancellationException) {
                if (curMessageId != null) {
                    val aIMessages = _messages.value
                    val index = aIMessages.indexOfFirst { it.id == curMessageId }
                    if (index < 0) return@launch
                    val list = aIMessages.toMutableList()
                    if (TextUtils.equals(list[index].content, "AI 正在思考中...")) {
                        list[index] = list[index].copy(content = "已停止生成")
                    }
                    _messages.value = list
                }
            } catch (e: Exception) {
                val currentMessages = _messages.value
                val index = currentMessages.indexOfFirst {
                    it.id == assistantMessageId
                }

                if (index >= 0) {
                    val newList = currentMessages.toMutableList()
                    newList[index] = newList[index].copy(
                        content = "请求失败：${e.message ?: "未知错误"}"
                    )
                    _messages.value = newList
                }

            } finally {
                _thinking.value = false
                _loading.value = false
                curMessageId = null
            }
        }
    }

    fun stopGenerate() {

        streamJob?.cancel()
        streamJob = null

        _thinking.value = false
        _loading.value = false
    }

    fun newChat() {
        if (_loading.value) return
        _currentConversationId.value = null
        _messages.value = emptyList()
    }

    // 最近会话列表
    private val _conversations = MutableStateFlow<Pair<Boolean,List<AIChatConversation>>>(Pair(true, emptyList()))
    val conversations = _conversations.asStateFlow()

    /**
     * 加载最近会话
     */
    fun loadCurConversations(initial: Boolean = false) {
        viewModelScope.launch {
            repository.loadCurConversations().asResult().collectLatest { result ->
                when (result) {
                    is ApiResult.Success -> {
                        val data = result.data ?: emptyList()
                        Collections.reverse(data)
                        _conversations.value = Pair(initial, data)

                    }

                    is ApiResult.Error -> {
                        _conversations.value = Pair(initial, emptyList())
                            listOf(AIChatConversation(title = "请求失败：${result.msg}"))
                    }
                }
            }
        }
    }

    /**
     * 加载指定会话的消息
     */
    fun loadMessagesByCid(conversationId: Long) {
        this._currentConversationId.value = conversationId
        viewModelScope.launch {
            repository.loadMessagesByCid(conversationId).asResult().collectLatest { result ->
                when (result) {
                    is ApiResult.Success -> {
                        _messages.value = (result.data ?: emptyList()).map { item ->
                            AIChatMessage(
                                id = item.id,
                                content = item.content,
                                isUser = TextUtils.equals(item.role, "user")
                            )
                        }
                    }

                    is ApiResult.Error -> {
                        _messages.value = listOf(
                            AIChatMessage(
                                content = "请求失败：${result.msg}", isUser = false
                            )
                        )
                    }
                }
            }
        }
    }

    fun renameConversation(conversationId: Long, title: String) {
        viewModelScope.launch {
            repository.updateConversationTitle(conversationId, title).asResult()
                .collectLatest { result ->
                    when (result) {
                        is ApiResult.Success -> {
                            toast.emit("修改成功")
                            loadCurConversations()
                        }

                        is ApiResult.Error -> {
                            toast.emit("修改失败: ${result.msg}")
                        }
                    }
                }
        }
    }

    fun deleteConversation(conversationId: Long) {
        viewModelScope.launch {
            repository.deleteConversation(conversationId).asResult().collectLatest { result ->
                when (result) {
                    is ApiResult.Success -> {
                        toast.emit("删除成功")
                        loadCurConversations()
                    }

                    is ApiResult.Error -> {
                        toast.emit("删除失败: ${result.msg}")
                    }
                }
            }
        }
    }

}
