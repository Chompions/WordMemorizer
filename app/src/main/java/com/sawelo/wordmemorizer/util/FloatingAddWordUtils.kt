package com.sawelo.wordmemorizer.util

import com.sawelo.wordmemorizer.data.WordRepository
import com.sawelo.wordmemorizer.data.data_class.DataItem
import com.sawelo.wordmemorizer.data.data_class.Word
import com.sawelo.wordmemorizer.data.data_class.WordWithCategories
import kotlinx.coroutines.flow.*

class FloatingAddWordUtils(
    private val wordRepository: WordRepository,
) {
    val wordTextFlow = MutableStateFlow("")
    val furiganaTextFlow = MutableStateFlow("")
    val definitionTextFlow = MutableStateFlow("")

    var latestTextInput = ""

    suspend fun getAllCategories() = wordRepository.getAllCategories().first()

    fun getAllWordsByTextFlow() = channelFlow {
        merge(
            wordTextFlow, furiganaTextFlow, definitionTextFlow
        ).collectLatest {
            latestTextInput = it
            send(
                wordRepository.getAllWordsByText(
                    wordTextFlow.value, furiganaTextFlow.value, definitionTextFlow.value
                )
            )
        }
    }

    suspend fun getTranslatedWord(): String? {
        return if (latestTextInput.isNotBlank()) {
            return wordRepository.translateWordFromLingvanex(latestTextInput)?.result
        } else {
            null
        }
    }

    suspend fun getRecommendationsWords(): List<DataItem>? {
        return if (latestTextInput.isNotBlank()) {
            return wordRepository.searchWordFromJisho(latestTextInput)?.data
        } else {
            null
        }
    }

    suspend fun addWord(wordWithCategories: WordWithCategories) {
        try {
            wordRepository.addWordWithCategories(wordWithCategories)
        } catch (e: Exception) {
            throw Exception("Unable to add your word: ${e.message}")
        }
    }

    suspend fun updateShowForgotWord(word: Word) {
        wordRepository.updateShowForgotWord(word)
    }
}