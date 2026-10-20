package com.treasure.restart.network.repository

import com.treasure.basic.network.RetrofitClient
import com.treasure.basic.network.safeApiCall
import com.treasure.restart.network.api.ExtApi

class ExtRepository {
    private val api = RetrofitClient.getInstance().create(ExtApi::class.java)

    suspend fun getCarouselList() = safeApiCall {
        api.getCarouselList()
    }
}