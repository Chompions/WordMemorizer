package com.sawelo.wordmemorizer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.recyclerview.widget.AsyncDifferConfig
import com.atilika.kuromoji.jumandic.Tokenizer
import com.sawelo.wordmemorizer.data.Category
import com.sawelo.wordmemorizer.data.Word
import com.sawelo.wordmemorizer.data.WordRepository
import com.sawelo.wordmemorizer.utils.CategoryDiffUtilCallback
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

    fun getAllWordsPagingData(): Flow<PagingData<Word>> {
        return wordRepository.getAllWordsPagingData().cachedIn(viewModelScope)
    }

    fun getAllWordsByCategoryPagingData(category: Category): Flow<PagingData<Word>> {
        return wordRepository.getAllWordsByCategoryPagingData(category).cachedIn(viewModelScope)
    }

    fun getAllForgottenWordsPagingData(): Flow<PagingData<Word>> {
        return wordRepository.getAllForgottenWordsPagingData().cachedIn(viewModelScope)
    }

    fun getAllCategories(): Flow<List<Category>> {
        return wordRepository.getAllCategories()
    }

//    fun getAllWordsSizeByCategory(category: Category, result: (Int) -> Unit) {
//        viewModelScope.launch {
//            result.invoke(wordRepository.getAllWordsSizeByCategory(category))
//        }
//    }

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


//    fun getAllWordsByCategory(category: Category? = null): LiveData<List<Word>> {
//        return when {
//            category?.isAll() == true -> wordRepository.getAllWordsByAll().asLiveData()
//            category == null -> wordRepository.getAllWordsByAll().asLiveData()
//            else -> wordRepository.getAllWordsByCategory(category).asLiveData()
//        }
//    }
//    fun getAllWordsByWord(inputString: String): LiveData<List<Word>> =
//        wordRepository.getAllWordsByWord(inputString).asLiveData()
//

//    }

//


//

//

//


}