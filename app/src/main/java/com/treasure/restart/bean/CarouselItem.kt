package com.treasure.restart.bean

data class CarouselItem(
    val id: Long = 0,
    val momentId: Long = 0,
    val imageUrl: String = "",
    val sort: Int = 0,
    val createdAt: String = "",
    val momentContent: String = "",
    val createUserName: String = "",
    val createUserAvatar: String = ""
)