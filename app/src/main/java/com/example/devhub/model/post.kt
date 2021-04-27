package com.example.devhub.model

data class Posts(
    var creation_time: Long = 0,
    var description: String = "",
    var image_url: String = "",
    var user: Users? = null,
    var doots: Int = 0,
    var PostId:String = ""
)