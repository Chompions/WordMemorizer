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
        localRepository.updateWordWithCategories(oldData, newData)
    }

    suspend fun deleteWord(word: Word) {
        localRepository.deleteWord(word)
    }

    suspend fun getWordWithCategoriesById(wordId: Int): WordWithCategories {
        return localRepository.getWordWithCategoriesById(wordId)
    }
}