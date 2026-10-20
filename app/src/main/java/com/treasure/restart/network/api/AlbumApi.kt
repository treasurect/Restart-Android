package com.treasure.restart.network.api

import com.treasure.basic.network.BaseResponse
import com.treasure.restart.bean.AlbumListResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface AlbumApi {

    @GET("api/album/list")
    suspend fun getAlbumList(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): BaseResponse<AlbumListResponse>
}
