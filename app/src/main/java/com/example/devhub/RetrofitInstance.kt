package com.example.devhub

import com.example.devhub.model.Constants.Companion.BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {

    companion object{
        private val retrofit by lazy{

            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()


        }

        val api: NotificationAPI by lazy{
            retrofit.create(NotificationAPI::class.java)
        }
    }
}