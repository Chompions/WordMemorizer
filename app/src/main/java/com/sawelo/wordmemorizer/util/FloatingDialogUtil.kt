package com.sawelo.wordmemorizer.util

import com.sawelo.wordmemorizer.data.WordRepository
import com.sawelo.wordmemorizer.data.data_class.DataItem
import com.sawelo.wordmemorizer.data.data_class.Word
import com.sawelo.wordmemorizer.data.data_class.WordWithCategories
import com.sawelo.wordmemorizer.data.remote.ApiConfig
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FloatingDialogUtil(private val wordRepository: WordRepository) {
    val wordTextFlow = MutableStateFlow("")
    val furiganaTextFlow = MutableStateFlow("")
    val definitionTextFlow = MutableStateFlow("")
    val progressIndicatorShowFlow = MutableStateFlow(false)

    suspend fun getAllCategories() = wordRepository.getAllCategories().first()

    fun getAllWordsByTextFlow() = channelFlow {
        merge(
            wordTextFlow, furiganaTextFlow, definitionTextFlow
        ).collectLatest {
            send(
                wordRepository.getAllWordsByText(
                    wordTextFlow.value, furiganaTextFlow.value, definitionTextFlow.value
                )
            )
        }
    }

    fun getRecommendationWordsFlow() = channelFlow {
        var wordTextList = emptyList<DataItem>()
        var furiganaTextList = emptyList<DataItem>()
        var definitionTextList = emptyList<DataItem>()

        suspend fun String.searchWord(method: (dataList: List<DataItem>) -> Unit) {
            if (this.isNotBlank()) {
                progressIndicatorShowFlow.value = true
                val textList = ApiConfig.getApiService().searchWord(this).data
                method.invoke(textList)
                send((wordTextList + furiganaTextList + definitionTextList))
            } else {
                wordTextList = emptyList()
                send((wordTextList + furiganaTextList + definitionTextList))
            }
            progressIndicatorShowFlow.value = false
        }

        launch {
            wordTextFlow.collectLatest {
                it.searchWord {dataList ->
                    wordTextList = dataList
                }
            }
        }

        launch {
            furiganaTextFlow.collectLatest {
                it.searchWord {dataList ->
                    furiganaTextList = dataList
                }
            }
        }

        launch {
            definitionTextFlow.collectLatest {
                it.searchWord {dataList ->
                    definitionTextList = dataList
                }
            }
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