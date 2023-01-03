package com.sawelo.wordmemorizer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.recyclerview.widget.AsyncDifferConfig
import com.atilika.kuromoji.jumandic.Tokenizer
import com.sawelo.wordmemorizer.data.WordRepository
import com.sawelo.wordmemorizer.data.data_class.Category
import com.sawelo.wordmemorizer.data.data_class.Word
import com.sawelo.wordmemorizer.util.callback.CategoryDiffUtilCallback
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val wordRepository: WordRepository
) : ViewModel() {
    val tokenizer = Tokenizer()
    val asyncDifferConfig = AsyncDifferConfig.Builder(CategoryDiffUtilCallback).build()
    var currentCategoryFragmentTag: String? = null

    fun getAllCategories(): Flow<List<Category>> {
        return wordRepository.getAllCategories()
    }

    fun getAllWordsPagingData(category: Category): Flow<PagingData<Word>> {
        return wordRepository.getAllWordsPagingData(category).cachedIn(viewModelScope)
    }

    fun getAllForgottenWordsPagingData(category: Category): Flow<PagingData<Word>> {
        return wordRepository.getAllForgottenWordsPagingData(category).cachedIn(viewModelScope)
    }

    fun getAllWordsByWord(wordText: String, result: (List<Word>) -> Unit) {
        viewModelScope.launch {
            result.invoke(wordRepository.getAllWordsByWord(wordText))
        }
    }

    fun addWord(word: Word) {
        viewModelScope.launch {
            wordRepository.addWord(word)
        }
    }

    fun addCategory(category: Category) {
        viewModelScope.launch {
            wordRepository.addCategory(category)
        }
    }

    fun updateForgotCountWord(word: Word) {
        viewModelScope.launch {
            wordRepository.updateForgotCountWord(word)
        }
    }

    fun updateIsForgottenWord(word: Word, isForgotten: Boolean) {
        viewModelScope.launch {
            wordRepository.updateIsForgottenWord(word, isForgotten)
        }
    }

    fun resetAllForgotCount() {
        viewModelScope.launch {
            wordRepository.resetAllForgotCount()
        }
    }

    fun deleteWord(word: Word) {
        viewModelScope.launch {
            wordRepository.deleteWord(word)
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            wordRepository.deleteCategory(category)
        }
    }

}