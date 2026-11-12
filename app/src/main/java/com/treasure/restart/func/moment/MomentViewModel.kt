package com.treasure.restart.func.moment

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.treasure.basic.base.BaseViewModel
import com.treasure.basic.network.ApiResult
import com.treasure.basic.network.asResult
import com.treasure.restart.bean.MomentItem
import com.treasure.restart.bean.MomentListResponse
import com.treasure.restart.bean.MomentPublishRequest
import com.treasure.restart.network.repository.MomentRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MomentViewModel(application: Application) : BaseViewModel(application) {

    private val repository = MomentRepository()

    companion object {
        private const val PAGE_SIZE = 10
    }

    // ==================== 发布朋友圈 ====================

    private val _publishResult = MutableSharedFlow<Long?>()
    val publishResult = _publishResult.asSharedFlow()

    fun publishMoment(content: String, location: String = "不公开", imageUrls: List<String> = emptyList()) {
        viewModelScope.launch {
            val request = MomentPublishRequest(
                content = content,
                location = location,
                images = imageUrls
            )

            repository.publishMoment(request).asResult().collectLatest {
                _publishResult.emit(if (it is ApiResult.Success) it.data else null)
            }
        }
    }

    private val _momentList = MutableStateFlow<List<MomentItem>>(emptyList())
    val momentList = _momentList.asStateFlow()
    private val _listState = MutableSharedFlow<Pair<Int, Boolean>>()
    val listState = _listState.asSharedFlow()
    private val _currentPage = MutableStateFlow(1)

    fun loadMomentList(refresh: Boolean = false) {
        viewModelScope.launch {
            val page = if (refresh) 1 else _currentPage.value
            try {
                repository.getMomentList(page, PAGE_SIZE).asResult().collectLatest {response->
                    when(response) {
                        is ApiResult.Success -> {
                            val data = response.data ?: MomentListResponse(0, 0,0L,0L,false, emptyList())
                            if (refresh) {
                                _momentList.value = data.momentList
                            } else {
                                val oldList = _momentList.value
                                _momentList.value = oldList + data.momentList
                            }
                            // 下一次请求的页码
                            _currentPage.value = data.page + 1
                            _listState.emit(Pair(if (refresh) 1 else 2, data.hasMore))
                        }
                        is ApiResult.Error -> {
                            toast.emit(response.msg.ifBlank { "加载失败" })
                            _listState.emit(Pair(if (refresh) 1 else 2, false))
                        }
                    }
                }


            } catch (e: Exception) {
                toast.emit(e.message ?: "网络请求失败")
            } finally {
            }
        }
    }

    /**
     * 清空朋友圈数据
     *
     * 一般不需要主动调用。
     * 如果以后切换账号，可以使用。
     */
    fun clearMomentList() {
        _momentList.value = emptyList()
        _currentPage.value = 1
    }
}