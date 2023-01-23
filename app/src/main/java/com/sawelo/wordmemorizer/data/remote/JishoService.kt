package com.sawelo.wordmemorizer.data.remote

import com.sawelo.wordmemorizer.data.data_class.JishoResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface JishoService {
    @GET ("/api/v1/search/words")
    suspend fun searchWord(
        @Query("keyword") text: String
    ): JishoResponse
}