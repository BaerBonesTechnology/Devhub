package com.example.devhub.com.example.devhub.model

import com.example.devhub.model.Users

data class Comment (
    var user: Users? = null,
    var comment_time_Created: Long = 0,
    var comment_Content:String="",
    var comment_doots: Long = 0
)