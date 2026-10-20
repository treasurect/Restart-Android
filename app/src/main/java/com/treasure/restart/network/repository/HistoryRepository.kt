package com.treasure.restart.network.repository

import com.treasure.basic.network.RetrofitClient
import com.treasure.basic.network.safeApiCall
import com.treasure.restart.network.api.HistoryApi

class HistoryRepository {

    private val api = RetrofitClient.getInstance().create(HistoryApi::class.java)

    suspend fun getHistoryList(region: String, page: Int, pageSize: Int) = safeApiCall {
        api.getHistoryList(region, page, pageSize)
    }
}
