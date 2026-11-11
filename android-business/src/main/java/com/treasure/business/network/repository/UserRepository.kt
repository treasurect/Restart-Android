package com.treasure.business.network.repository

import com.treasure.basic.network.RetrofitClient
import com.treasure.basic.network.safeApiCall
import com.treasure.basic.utils.Ext.toJsonBody
import com.treasure.business.network.api.UserApi

class UserRepository {
    private val api = RetrofitClient.getInstance().create(UserApi::class.java)

    suspend fun actionLoginCode(username: String, pwd: String) = safeApiCall {
            api.actionLoginPwd(
                hashMapOf<String, Any>(
                    "username" to username,
                    "password" to pwd,
                    "nickname" to username
                ).toJsonBody()
            )
        }

    suspend fun getUserInfo() = safeApiCall {
        api.getUserInfo()
    }
}
