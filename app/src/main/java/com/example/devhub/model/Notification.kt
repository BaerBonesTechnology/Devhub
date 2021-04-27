package com.example.devhub.com.example.devhub.model

import com.example.devhub.model.Posts
import com.example.devhub.model.Users

data class Notification (
    var fromUser: Users? = null,
    var userPost: Posts? = null,
    var actions: String = "",
    var time: Long = 0
        )