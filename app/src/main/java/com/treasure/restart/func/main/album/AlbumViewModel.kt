package com.treasure.restart.func.main.album

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.treasure.basic.base.BaseViewModel
import com.treasure.basic.network.ApiResult
import com.treasure.basic.network.asResult
import com.treasure.restart.bean.AlbumItem
import com.treasure.restart.bean.AlbumListResponse
import com.treasure.restart.network.repository.AlbumRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AlbumViewModel(application: Application) : BaseViewModel(application) {

    private val repository = AlbumRepository()

    companion object {
        private const val PAGE_SIZE = 10
    }

    data class AlbumListState(
        val items: List<AlbumItem> = emptyList(),
        val page: Int = 1,
        val hasMore: Boolean = true,
        val isRefreshing: Boolean = false,
        val isLoadingMore: Boolean = false,
        val error: String? = null
    )

    private val _state = MutableStateFlow(AlbumListState())
    val state: StateFlow<AlbumListState> = _state.asStateFlow()

    /**
     * 拉取相册列表
     *
     * @param refresh true 重新加载第一页，false 加载下一页
     */
    fun loadAlbums(refresh: Boolean) {
        val current = _state.value
        if (current.isRefreshing || current.isLoadingMore) return
        if (!refresh && !current.hasMore) return

        val targetPage = if (refresh) 1 else current.page
        val pending = if (refresh) {
            current.copy(isRefreshing = true, error = null)
        } else {
            current.copy(isLoadingMore = true, error = null)
        }
        _state.value = pending

        viewModelScope.launch {
            repository.getAlbumList(targetPage, PAGE_SIZE).asResult().collectLatest { result ->
                when (result) {
                    is ApiResult.Success -> {
                        val data = result.data ?: AlbumListResponse()
                        val records = data.imageList.orEmpty().filterNotNull()
                        val latest = _state.value
                        val items = if (refresh) records else latest.items + records
                        val total = data.total ?: 0L
                        val hasMore = if (total > 0) {
                            items.size.toLong() < total
                        } else {
                            records.isNotEmpty()
                        }
                        _state.update {
                            it.copy(
                                items = items,
                                page = targetPage + 1,
                                hasMore = hasMore,
                                isRefreshing = false,
                                isLoadingMore = false,
                                error = null
                            )
                        }
                    }

                    is ApiResult.Error -> {
                        toast.emit(result.msg.ifBlank { "加载失败" })
                        _state.update {
                            it.copy(isRefreshing = false, isLoadingMore = false, error = result.msg)
                        }
                    }
                }
            }
        }
    }
}
