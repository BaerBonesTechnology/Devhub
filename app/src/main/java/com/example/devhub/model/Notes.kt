package com.example.devhub.com.example.devhub.model

import com.example.devhub.model.Users


data class Notes(
        var user:Users? = null,
        var note_title:String = "",
        var note_time_Created: Long = 0,
        var note_Content:String=""

)
