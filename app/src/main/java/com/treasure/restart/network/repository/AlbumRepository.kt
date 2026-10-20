package com.treasure.restart.network.repository

import com.treasure.basic.network.RetrofitClient
import com.treasure.basic.network.safeApiCall
import com.treasure.restart.network.api.AlbumApi

class AlbumRepository {

    private val api = RetrofitClient.getInstance().create(AlbumApi::class.java)

    suspend fun getAlbumList(page: Int, pageSize: Int) = safeApiCall {
        api.getAlbumList(page, pageSize)
    }
}
