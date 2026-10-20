package com.treasure.restart.func.main.home

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.treasure.basic.base.BaseViewModel
import com.treasure.basic.bean.CarouselItem
import com.treasure.basic.network.ApiResult
import com.treasure.basic.network.asResult
import com.treasure.restart.bean.HistoryItem
import com.treasure.restart.network.repository.ExtRepository
import com.treasure.restart.network.repository.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : BaseViewModel(application) {
    private val historyRepository = HistoryRepository()
    private val extRepository: ExtRepository = ExtRepository()

    companion object {
        private const val PAGE_SIZE = 10
    }

    data class HistoryPageState(
        val items: List<HistoryItem> = emptyList(),
        val page: Int = 1,
        val hasMore: Boolean = true,
        val isRefreshing: Boolean = false,
        val isLoadingMore: Boolean = false,
        val error: String? = null
    )

    private val _historyStates = MutableStateFlow<Map<String, HistoryPageState>>(emptyMap())
    val historyStates: StateFlow<Map<String, HistoryPageState>> = _historyStates.asStateFlow()

    fun historyState(region: String) =
        historyStates.map { it[region] ?: HistoryPageState() }.distinctUntilChanged()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    /**
     * 拉取历史内容列表
     *
     * @param region 与 HomeChildFragment 的 page_type 保持一致
     * @param refresh true 重新加载第一页，false 加载下一页
     */
    fun loadHistory(region: String, refresh: Boolean) {
        val current = _historyStates.value[region] ?: HistoryPageState()
        if (current.isRefreshing || current.isLoadingMore) return
        if (!refresh && !current.hasMore) return

        val targetPage = if (refresh) 1 else current.page
        val pending = if (refresh) {
            current.copy(isRefreshing = true, error = null)
        } else {
            current.copy(isLoadingMore = true, error = null)
        }
        _historyStates.update { it + (region to pending) }

        _loading.value = true
        viewModelScope.launch {
            historyRepository.getHistoryList(region, targetPage, PAGE_SIZE).asResult()
                .collectLatest { result ->
                    when (result) {
                        is ApiResult.Success -> {
                            val data = result.data ?: arrayListOf()
                            val latest = _historyStates.value[region] ?: pending
                            val items = if (refresh) data else latest.items + data
                            val hasMore = false
                            _historyStates.update {
                                it + (region to HistoryPageState(
                                    items = items,
                                    page = targetPage + 1,
                                    hasMore = hasMore,
                                    isRefreshing = false,
                                    isLoadingMore = false,
                                    error = null
                                ))
                            }
                            _loading.value = false
                        }

                        is ApiResult.Error -> {
                            toast.emit(result.msg.ifBlank { "加载失败" })
                            val latest = _historyStates.value[region] ?: pending
                            _historyStates.update {
                                it + (region to latest.copy(
                                    isRefreshing = false, isLoadingMore = false, error = result.msg
                                ))
                            }
                            _loading.value = false
                        }
                    }
                }
        }
    }

    private val _items = MutableStateFlow<List<CarouselItem>>(emptyList())
    val carouselList: StateFlow<List<CarouselItem>> = _items.asStateFlow()
    fun loadCarousel() {
        viewModelScope.launch {
            try {
                extRepository.getCarouselList().asResult().collectLatest { result ->
                    _items.emit(
                        when (result) {
                        is ApiResult.Success -> result.data?.map {
                            CarouselItem(it.momentContent, it.imageUrl)
                        } ?: mutableListOf()

                        is ApiResult.Error -> mutableListOf()
                    })
                }
            } catch (e: Exception) {
                toast.emit(e.message ?: "轮播图加载失败")
            }
        }
    }
}
