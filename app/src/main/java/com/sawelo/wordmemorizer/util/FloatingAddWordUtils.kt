package com.sawelo.wordmemorizer.util

import com.atilika.kuromoji.ipadic.Tokenizer
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.sawelo.wordmemorizer.data.WordRepository
import com.sawelo.wordmemorizer.data.data_class.BaseWord
import com.sawelo.wordmemorizer.data.data_class.Word
import com.sawelo.wordmemorizer.data.data_class.WordWithCategories
import dev.esnault.wanakana.core.Wanakana
import kotlinx.coroutines.flow.*

class FloatingAddWordUtils(
    private val wordRepository: WordRepository,
) {
    private val wordTextFlow = MutableStateFlow("")
    private val furiganaTextFlow = MutableStateFlow("")
    private val definitionTextFlow = MutableStateFlow("")

    private var translatorClient: Translator? = null

    var focusedTextInput = InputType.WORD_INPUT

    private fun getFocusedWordText(): String {
        return when (focusedTextInput) {
            InputType.WORD_INPUT -> wordTextFlow.value
            InputType.FURIGANA_INPUT -> furiganaTextFlow.value
            InputType.DEFINITION_INPUT -> definitionTextFlow.value
        }
    }

    fun setWordFlow(inputType: InputType, text: String) {
        when (inputType) {
            InputType.WORD_INPUT -> wordTextFlow.value = text
            InputType.FURIGANA_INPUT -> furiganaTextFlow.value = text
            InputType.DEFINITION_INPUT -> definitionTextFlow.value = text
        }
    }

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

    fun getTranslatedWord(result: (BaseWord?) -> Unit) {
        val tokenizer = Tokenizer()
        val focusedText = getFocusedWordText()
        if (focusedText.isNotBlank()) {
            if (Wanakana.isJapanese(focusedText)) {
                val options = TranslatorOptions.Builder()
                    .setSourceLanguage(TranslateLanguage.JAPANESE)
                    .setTargetLanguage(TranslateLanguage.ENGLISH)
                    .build()
                translatorClient = Translation.getClient(options)
                translatorClient?.translate(focusedText)
                    ?.addOnSuccessListener { translatedText ->
                        val readingText = tokenizer.tokenize(focusedText)
                            .joinToString(", ") {
                                it.reading
                            }
                        val baseWord = BaseWord(
                            focusedText,
                            Wanakana.toHiragana(readingText),
                            translatedText
                        )
                        result.invoke(baseWord)
                    }
                    ?.addOnFailureListener {
                        result.invoke(null)
                    }
            } else {
                val options = TranslatorOptions.Builder()
                    .setSourceLanguage(TranslateLanguage.ENGLISH)
                    .setTargetLanguage(TranslateLanguage.JAPANESE)
                    .build()
                translatorClient = Translation.getClient(options)
                translatorClient?.translate(focusedText)
                    ?.addOnSuccessListener { translatedText ->
                        val readingText = tokenizer.tokenize(translatedText)
                            .joinToString(" , ") {
                                it.reading
                            }
                        val baseWord = BaseWord(
                            translatedText,
                            Wanakana.toHiragana(readingText),
                            focusedText
                        )
                        result.invoke(baseWord)
                    }
                    ?.addOnFailureListener {
                        result.invoke(null)
                    }
            }
        } else {
            result.invoke(null)
        }
    }

    fun destroyUtils() {
        translatorClient?.close()
        translatorClient = null
    }

    suspend fun getAllCategories() = wordRepository.getAllCategories().first()

    suspend fun getRecommendationsWords(result: (List<BaseWord>?) -> Unit) {
        val focusedText = getFocusedWordText()
        if (focusedText.isNotBlank()) {
            val baseWordList = mutableListOf<BaseWord>()
            wordRepository.searchWordFromJisho(focusedText)?.data?.forEach { data ->
                val wordText = data.japanese?.first()?.word
                val furiganaText = data.japanese?.first()?.reading
                val definitionText =
                    data.senses?.first()?.englishDefinitions?.joinToString(" / ")

                val baseWord = BaseWord(
                    wordText ?: "",
                    furiganaText ?: "",
                    definitionText ?: ""
                )
                baseWordList.add(baseWord)
            }
            result.invoke(baseWordList)
        } else {
            result.invoke(null)
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