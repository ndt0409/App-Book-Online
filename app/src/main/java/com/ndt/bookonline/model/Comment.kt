package com.ndt.bookonline.model

data class Comment(
    var id: String = "",
    var bookId: String = "",
    var timestamp: String = "",
    var comment: String = "",
    var uid: String = ""
)