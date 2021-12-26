package com.ndt.bookonline.model

data class PDF(
    var uid: String = "",
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var categoryId: String = "",
    var url: String = "",
    var timestamp: Long = 0,
    var viewCount: Long = 0,
    var dowloadsCount: Long = 0
)