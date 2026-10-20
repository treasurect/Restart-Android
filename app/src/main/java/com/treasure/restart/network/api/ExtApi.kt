package com.treasure.restart.network.api

import com.treasure.basic.network.BaseResponse
import com.treasure.restart.bean.CarouselItem
import retrofit2.http.GET

interface ExtApi {

    @GET("api/ext/carousel/list")
    suspend fun getCarouselList(): BaseResponse<List<CarouselItem>>
}