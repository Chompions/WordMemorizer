package com.sawelo.wordmemorizer.viewmodel

import androidx.lifecycle.ViewModel
import com.sawelo.wordmemorizer.data.data_class.entity.Word
import com.sawelo.wordmemorizer.data.data_class.relation_ref.CategoryWithInfo
import com.sawelo.wordmemorizer.data.data_class.relation_ref.WordWithCategories
import com.sawelo.wordmemorizer.data.repository.LocalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class UpdateWordViewModel @Inject constructor(
    private val localRepository: LocalRepository
) : ViewModel() {

    fun getAllCategories(): Flow<List<CategoryWithInfo>> {
        return localRepository.getAllCategoryWithInfo()
    }

    suspend fun updateWord(
        oldData: WordWithCategories,
        newData: WordWithCategories
    ) {
        try {
            localRepository.updateWordWithCategories(oldData, newData)
        } catch (e: Exception) {
            throw Exception("Unable to update your word: ${e.message}")
        }
    }

    suspend fun deleteWord(word: Word) {
        try {
            localRepository.deleteWord(word)
        } catch (e: Exception) {
            throw Exception("Unable to delete your word: ${e.message}")
        }
    }

    suspend fun getWordWithCategoriesById(wordId: Int): WordWithCategories {
        try {
            return localRepository.getWordWithCategoriesById(wordId)
        } catch (e: Exception) {
            throw Exception("Unable to get word details: ${e.message}")
        }
    }
}