package com.baerhous.devhub.model

data class UserNotification (
    val body:String = "",
    val title: String = "",
    val time: Long = 0,
    val postID: String = ""
        )