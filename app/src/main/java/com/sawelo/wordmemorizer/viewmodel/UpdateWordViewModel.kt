package com.sawelo.wordmemorizer.viewmodel

import androidx.lifecycle.ViewModel
import com.sawelo.wordmemorizer.data.WordRepository
import com.sawelo.wordmemorizer.data.data_class.Word
import com.sawelo.wordmemorizer.data.data_class.WordWithCategories
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UpdateWordViewModel @Inject constructor(
    private val wordRepository: WordRepository
) : ViewModel() {

    suspend fun updateWord(
        oldData: WordWithCategories,
        newData: WordWithCategories
    ) {
        try {
            wordRepository.updateWordWithCategories(oldData, newData)
        } catch (e: Exception) {
            throw Exception("Unable to update your word: ${e.message}")
        }
    }

    suspend fun deleteWord(word: Word) {
        try {
            wordRepository.deleteWord(word)
        } catch (e: Exception) {
            throw Exception("Unable to delete your word: ${e.message}")
        }
    }

    suspend fun getWordWithCategoriesById(wordId: Int): WordWithCategories {
        try {
            return wordRepository.getWordWithCategoriesById(wordId)
        } catch (e: Exception) {
            throw Exception("Unable to get word details: ${e.message}")
        }
    }
}