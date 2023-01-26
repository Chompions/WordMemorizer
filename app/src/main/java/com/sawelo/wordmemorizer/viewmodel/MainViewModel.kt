package com.sawelo.wordmemorizer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.recyclerview.widget.AsyncDifferConfig
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
    val asyncDifferConfig = AsyncDifferConfig.Builder(CategoryDiffUtilCallback).build()

    fun getAllCategories(): Flow<List<Category>> {
        return wordRepository.getAllCategories()
    }

    fun getAllWordsPagingData(category: Category): Flow<PagingData<Word>> {
        return wordRepository.getAllWordsPagingData(category).cachedIn(viewModelScope)
    }

    fun getAllForgottenWordsPagingData(category: Category): Flow<PagingData<Word>> {
        return wordRepository.getAllForgottenWordsPagingData(category).cachedIn(viewModelScope)
    }

    fun addCategory(category: Category) {
        viewModelScope.launch {
            wordRepository.addCategory(category)
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            wordRepository.deleteCategory(category)
        }
    }

    fun updateShowForgotWord(word: Word) {
        viewModelScope.launch {
            wordRepository.updateShowForgotWord(word)
        }
    }

    fun updateHideForgotWord(word: Word) {
        viewModelScope.launch {
            wordRepository.updateHideForgotWord(word)
        }
    }

    fun resetAllForgotCount() {
        viewModelScope.launch {
            wordRepository.resetAllForgotCount()
        }
    }

}