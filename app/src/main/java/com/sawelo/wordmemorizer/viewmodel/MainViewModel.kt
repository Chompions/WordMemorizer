package com.sawelo.wordmemorizer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.atilika.kuromoji.jumandic.Tokenizer
import com.sawelo.wordmemorizer.data.Category
import com.sawelo.wordmemorizer.data.Word
import com.sawelo.wordmemorizer.data.WordRepository
import com.sawelo.wordmemorizer.utils.WordUtils.isAll
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val wordRepository: WordRepository
) : ViewModel() {
    val tokenizer = Tokenizer()
    var currentCategoryFragmentTag: String? = null

    fun getAllWordsPagingData(id: Int? = null): Flow<PagingData<Word>> {
        return wordRepository.getAllWordsPagingData(id).cachedIn(viewModelScope)
    }

    fun getAllCategories(): LiveData<List<Category>> =
        wordRepository.getAllCategories().asLiveData()
    fun getAllWordsByCategory(category: Category? = null): LiveData<List<Word>> {
        return when {
            category?.isAll() == true -> wordRepository.getAllWordsByAll().asLiveData()
            category == null -> wordRepository.getAllWordsByAll().asLiveData()
            else -> wordRepository.getAllWordsByCategory(category).asLiveData()
        }
    }
    fun getAllWordsByWord(inputString: String): LiveData<List<Word>> =
        wordRepository.getAllWordsByWord(inputString).asLiveData()

    fun addWord(word: Word, callback: ((Int) -> Unit)) {
        viewModelScope.launch {
            wordRepository.addWord(word, callback)
        }
    }
    fun addCategory(category: Category, callback: ((Int) -> Unit)) {
        viewModelScope.launch {
            wordRepository.addCategory(category, callback)
        }
    }

    fun updateForgotCountWord(word: Word, callback: () -> Unit) {
        viewModelScope.launch {
            wordRepository.updateForgotCountWord(word, callback)
        }
    }
    fun updateIsForgottenWord(word: Word, isForgotten: Boolean, callback: () -> Unit) {
        viewModelScope.launch {
            wordRepository.updateIsForgottenWord(word, isForgotten, callback)
        }
    }

    fun resetAllForgotCount(callback: () -> Unit) {
        viewModelScope.launch {
            wordRepository.resetAllForgotCount(callback)
        }
    }

    fun deleteWord(word: Word, callback: () -> Unit) {
        viewModelScope.launch {
            wordRepository.deleteWord(word, callback)
        }
    }

    fun deleteCategory(category: Category, callback: () -> Unit) {
        viewModelScope.launch {
            wordRepository.deleteCategory(category, callback)
        }
    }

}