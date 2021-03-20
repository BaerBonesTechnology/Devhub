package com.example.devhub.Model

import com.google.firebase.firestore.PropertyName

data class Posts(
    @get:PropertyName("creation_time") @set:PropertyName("creation_time")var creation_time: Long = 0,
    var description: String = "",
    @get:PropertyName("image_url") @set:PropertyName("image_url") var image_url: String = "",
    var user: Users? = null
) {
}