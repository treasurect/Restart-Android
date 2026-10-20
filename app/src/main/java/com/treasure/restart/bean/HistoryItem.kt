package com.treasure.restart.bean

import java.io.Serializable

data class HistoryItem(
    val title: String = "",
    val description: String = "",
    val content: String = "",
    val button: String = "",
    val imageList: List<String> = emptyList()
) : Serializable
