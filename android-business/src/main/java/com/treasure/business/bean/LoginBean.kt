package com.treasure.business.bean

data class LoginBean(
    val nickname: String,
    val token: String,
    val userId: Long,
    val username: String
)