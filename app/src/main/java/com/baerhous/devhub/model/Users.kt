package com.baerhous.devhub.model

data class Users(
    var username: String = "",
    var age: Int = 0,
    var doots: Int = 0,
    var posts: Int = 0,
    var bio: String = "",
    var ProfilePicture_url: String = "",
    var userID:String = "",
    var FCM: String = "",
    var FIAM:String = ""
    )