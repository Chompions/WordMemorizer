package com.sawelo.wordmemorizer.util

import android.text.InputType
import com.atilika.kuromoji.ipadic.Tokenizer
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.sawelo.wordmemorizer.data.data_class.entity.Word
import com.sawelo.wordmemorizer.data.data_class.relation_ref.WordWithCategories
import com.sawelo.wordmemorizer.data.repository.LocalRepository
import com.sawelo.wordmemorizer.data.repository.RemoteRepository
import dev.esnault.wanakana.core.Wanakana
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FloatingDialogUtils(
    private val localRepository: LocalRepository,
    private val remoteRepository: RemoteRepository,
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
            val word = Word(
                wordText = wordTextFlow.value,
                furiganaText = furiganaTextFlow.value,
                definitionText = definitionTextFlow.value
            )
            send(localRepository.getAllWordsByText(word))
        }
    }

    suspend fun getTranslatedWord(): Word = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
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
                            val readingText = if (!Wanakana.isKatakana(focusedText)) {
                                tokenizer.tokenize(focusedText)
                                    .joinToString(",") { it.reading }
                            } else focusedText
                            val baseWord = Word(
                                wordText = focusedText,
                                furiganaText = Wanakana.toHiragana(readingText),
                                definitionText = translatedText
                            )
                            continuation.resume(baseWord)
                        }
                        ?.addOnFailureListener {
                            continuation.resumeWithException(it)
                        }
                } else {
                    val options = TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.ENGLISH)
                        .setTargetLanguage(TranslateLanguage.JAPANESE)
                        .build()
                    translatorClient = Translation.getClient(options)
                    translatorClient?.translate(focusedText)
                        ?.addOnSuccessListener { translatedText ->
                            val readingText = if (!Wanakana.isKatakana(focusedText)) {
                                tokenizer.tokenize(focusedText)
                                    .joinToString(",") { it.reading }
                            } else focusedText
                            val baseWord = Word(
                                wordText = translatedText,
                                furiganaText = Wanakana.toHiragana(readingText),
                                definitionText = focusedText
                            )
                            continuation.resume(baseWord)
                        }
                        ?.addOnFailureListener {
                            continuation.resumeWithException(it)
                        }
                }
            } else {
                continuation.resumeWithException(
                    Throwable("Focused text is empty")
                )
            }
        }
    }

    fun destroyUtils() {
        translatorClient?.close()
        translatorClient = null
        wordTextFlow.value = ""
        furiganaTextFlow.value = ""
        definitionTextFlow.value = ""
    }

    suspend fun getAllCategories() = localRepository.getAllCategory().first()

    suspend fun getRecommendationsWords(): List<Word> = withContext(Dispatchers.IO) {
        val focusedText = getFocusedWordText()
        return@withContext if (focusedText.isNotBlank()) {
            val baseWordList = mutableListOf<Word>()
            remoteRepository.searchWordFromJisho(focusedText)?.data?.forEach { data ->
                val wordText = data.japanese?.first()?.word
                val furiganaText = data.japanese?.first()?.reading
                val definitionText =
                    data.senses?.first()?.englishDefinitions?.joinToString(" / ")

                val baseWord = Word(
                    wordText = wordText ?: "",
                    furiganaText = furiganaText ?: "",
                    definitionText = definitionText ?: ""
                )
                baseWordList.add(baseWord)
            }
            baseWordList
        } else {
            throw Throwable("Focused text is empty")
        }
    }

    suspend fun addWord(wordWithCategories: WordWithCategories) {
        try {
            localRepository.addWordWithCategories(wordWithCategories)
        } catch (e: Exception) {
            throw Exception("Unable to add your word: ${e.message}")
        }
    }

    suspend fun updateShowForgotWord(word: Word) {
        localRepository.updateShowForgotWord(word)
    }

    enum class InputType {
        WORD_INPUT,
        FURIGANA_INPUT,
        DEFINITION_INPUT,
    }
}