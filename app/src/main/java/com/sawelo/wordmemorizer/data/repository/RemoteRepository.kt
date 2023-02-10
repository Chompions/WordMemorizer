package com.sawelo.wordmemorizer.data.repository

import com.sawelo.wordmemorizer.data.data_class.api_response.JishoResponse
import com.sawelo.wordmemorizer.data.remote.JishoService

class RemoteRepository(
    private val jishoService: JishoService,
) {
    suspend fun searchWordFromJisho(word: String): JishoResponse? {
        return jishoService.searchWord(word)
    }
}