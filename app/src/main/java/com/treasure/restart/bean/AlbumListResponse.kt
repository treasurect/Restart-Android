package com.treasure.restart.bean


data class AlbumListResponse(
    val total: Long? = null,
    val imageList: MutableList<AlbumItem?>? = null
)

data class AlbumItem(
    val id: Long = 0,
    val momentId: Long = 0,
    val momentContent: String = "",
    //发布者id
    val createUserId: Long = 0,
    val createUserName: String = "",
    val createUserAvatar: String = "",
    val imageUrl: String = "",
    //图片排序
    val sort: Int = 0,
    val createdAt: String = ""
)

