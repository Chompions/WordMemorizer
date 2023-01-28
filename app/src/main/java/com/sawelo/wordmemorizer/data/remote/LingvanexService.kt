package com.sawelo.wordmemorizer.data.remote

import com.sawelo.wordmemorizer.data.data_class.LingvanexBody
import com.sawelo.wordmemorizer.data.data_class.LingvanexResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface LingvanexService {
    @Headers(
        "X-RapidAPI-Key: 13567bdfe0mshbed8ef55d0d9fd8p110d1fjsne129752f8505",
        "Content-Type: application/json",
    )
    @POST ("/translate")
    suspend fun translateWord(
        @Body body: LingvanexBody
    ): LingvanexResponse?
}