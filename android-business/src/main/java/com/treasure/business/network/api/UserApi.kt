package com.treasure.business.network.api

import com.treasure.basic.network.BaseResponse
import com.treasure.business.bean.LoginBean
import com.treasure.business.bean.UserInfoBean
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface UserApi {
    @POST("api/user/loginPwd")
    suspend fun actionLoginPwd(
        @Body request: RequestBody
    ): BaseResponse<LoginBean>

    @GET("api/user/info")
    suspend fun getUserInfo(): BaseResponse<UserInfoBean>
}