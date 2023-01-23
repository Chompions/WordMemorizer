package com.sawelo.wordmemorizer.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiConfig {
    fun getApiService(): JishoService {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://jisho.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(JishoService::class.java)
    }
}