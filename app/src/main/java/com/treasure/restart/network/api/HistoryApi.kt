package com.treasure.restart.network.api

import com.treasure.basic.network.BaseResponse
import com.treasure.restart.bean.HistoryItem
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface HistoryApi {

    @GET("api/file/history/{region}")
    suspend fun getHistoryList(
        @Path("region") region: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): BaseResponse<List<HistoryItem>>
}
