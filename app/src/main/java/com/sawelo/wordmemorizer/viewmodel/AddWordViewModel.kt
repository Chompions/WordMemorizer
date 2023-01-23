package com.sawelo.wordmemorizer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sawelo.wordmemorizer.data.WordRepository
import com.sawelo.wordmemorizer.data.data_class.DataItem
import com.sawelo.wordmemorizer.data.data_class.Word
import com.sawelo.wordmemorizer.data.data_class.WordWithCategories
import com.sawelo.wordmemorizer.data.remote.ApiConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddWordViewModel @Inject constructor(
    private val wordRepository: WordRepository
) : ViewModel() {
    val wordTextFlow = MutableStateFlow("")
    val furiganaTextFlow = MutableStateFlow("")
    val definitionTextFlow = MutableStateFlow("")
    val progressIndicatorShowFlow = MutableStateFlow(false)

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

    fun addWord(wordWithCategories: WordWithCategories) {
        try {
            viewModelScope.launch {
                wordRepository.addWordWithCategories(wordWithCategories)
            }
        } catch (e: Exception) {
            throw Exception("Unable to add your word: ${e.message}")
        }
    }

    fun updateShowForgotWord(word: Word) {
        viewModelScope.launch {
            wordRepository.updateShowForgotWord(word)
        }
    }

    fun clearFlow() {
        wordTextFlow.value = ""
        furiganaTextFlow.value = ""
        definitionTextFlow.value = ""
    }
}