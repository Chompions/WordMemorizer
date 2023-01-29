package com.sawelo.wordmemorizer.util

import com.sawelo.wordmemorizer.data.WordRepository
import com.sawelo.wordmemorizer.data.data_class.DataItem
import com.sawelo.wordmemorizer.data.data_class.Word
import com.sawelo.wordmemorizer.data.data_class.WordWithCategories
import kotlinx.coroutines.flow.*

class FloatingAddWordUtils(
    private val wordRepository: WordRepository,
) {
    private val wordTextFlow = MutableStateFlow("")
    private val furiganaTextFlow = MutableStateFlow("")
    private val definitionTextFlow = MutableStateFlow("")

    var focusedTextInput = InputType.WORD_INPUT

    fun setWordFlow(inputType: InputType, text: String) {
        when (inputType) {
            InputType.WORD_INPUT -> wordTextFlow.value = text
            InputType.FURIGANA_INPUT -> furiganaTextFlow.value = text
            InputType.DEFINITION_INPUT -> definitionTextFlow.value = text
        }
    }

    fun getFocusedWordText(): String {
        return when (focusedTextInput) {
            InputType.WORD_INPUT -> wordTextFlow.value
            InputType.FURIGANA_INPUT -> furiganaTextFlow.value
            InputType.DEFINITION_INPUT -> definitionTextFlow.value
        }
    }

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

    suspend fun getTranslatedWord(): String? {
        val focusedText = getFocusedWordText()
        return if (focusedText.isNotBlank()) {
            return wordRepository.translateWordFromLingvanex(focusedText)?.result
        } else {
            null
        }
    }

    suspend fun getRecommendationsWords(): List<DataItem>? {
        val focusedText = getFocusedWordText()
        return if (focusedText.isNotBlank()) {
            return wordRepository.searchWordFromJisho(focusedText)?.data
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

    enum class InputType {
        WORD_INPUT,
        FURIGANA_INPUT,
        DEFINITION_INPUT,
    }
}