package com.treasure.restart

import android.app.Application
import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.treasure.basic.base.BaseViewModel
import com.treasure.basic.network.ApiResult
import com.treasure.basic.network.asResult
import com.treasure.restart.bean.WeeklyReportList
import com.treasure.restart.bean.FeedItem
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : BaseViewModel(application) {


    val categories = listOf("推荐", "RED", "热点", "直播", "短剧", "穿搭", "美甲")

    private val _feedItems = MutableLiveData<List<FeedItem>>(buildFeedItems())
    val feedItems: LiveData<List<FeedItem>> = _feedItems

    private fun buildFeedItems(): List<FeedItem> {
        return listOf(
            FeedItem(
                id = 1,
                title = "男人买车等级，你现在到哪一级了？",
                author = "阿吉分车",
                likeCount = "150",
                imageLabel = "买车等级",
                coverColor = Color.rgb(194, 55, 50)
            ),
            FeedItem(
                id = 2,
                title = "为了口吃的，陪比自己大25岁的睡觉",
                author = "泥泥嚎嚎",
                likeCount = "118",
                imageLabel = "真实故事",
                coverColor = Color.rgb(235, 166, 67)
            ),
            FeedItem(
                id = 3,
                title = "山东最美的20个地方，去过一半才不枉此生",
                author = "南說 · 中国旅游主页",
                likeCount = "1.5万",
                imageLabel = "山东旅行",
                coverColor = Color.rgb(60, 123, 92)
            ),
            FeedItem(
                id = 4,
                title = "人粮冻干教程",
                author = "纸飞机",
                likeCount = "0.8万",
                imageLabel = "宠物教程",
                coverColor = Color.rgb(84, 110, 182)
            )
        )
    }
}
