package com.sawelo.wordmemorizer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atilika.kuromoji.jumandic.Tokenizer
import com.sawelo.wordmemorizer.data.WordRepository
import com.sawelo.wordmemorizer.data.data_class.Word
import com.sawelo.wordmemorizer.data.data_class.WordWithCategories
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
    val tokenizer = Tokenizer()
    val wordTextFlow = MutableStateFlow("")
    val furiganaTextFlow = MutableStateFlow("")
    val definitionTextFlow = MutableStateFlow("")

    val getAllWordsByTextFlow = channelFlow {
        merge(
            wordTextFlow, furiganaTextFlow, definitionTextFlow
        ).collectLatest {
            send(wordRepository.getAllWordsByText(
                wordTextFlow.value, furiganaTextFlow.value, definitionTextFlow.value))
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