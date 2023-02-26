package com.sawelo.wordmemorizer.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.sawelo.wordmemorizer.data.data_class.entity.Category
import com.sawelo.wordmemorizer.data.data_class.entity.Word
import com.sawelo.wordmemorizer.data.data_class.relation_ref.CategoryWithInfo
import com.sawelo.wordmemorizer.data.data_class.relation_ref.WordWithInfo
import com.sawelo.wordmemorizer.data.repository.LocalRepository
import com.sawelo.wordmemorizer.ui.ui_util.ViewUtils.showToast
import com.sawelo.wordmemorizer.util.Constants.selectedCategories
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val localRepository: LocalRepository
) : ViewModel() {

    fun getAllCategoryWithInfo(): Flow<List<CategoryWithInfo>> {
        return localRepository.getAllCategoryWithInfo()
    }

    fun selectAllCategories() {
        viewModelScope.launch {
            selectedCategories.value = localRepository.getAllCategory().first()
        }
    }

    fun unselectAllCategories() {
        selectedCategories.value = emptyList()
    }

    fun getAllWordsPagingData(): Flow<PagingData<WordWithInfo>> = callbackFlow {
        selectedCategories.collectLatest { categoryList ->
            localRepository.getWordsByCategories(categoryList, false)
                .cachedIn(viewModelScope).collectLatest {
                    send(it)
                }
        }
        awaitClose { cancel() }
    }

    fun getAllForgottenWordsPagingData(): Flow<PagingData<WordWithInfo>> = callbackFlow {
        selectedCategories.collectLatest { categoryList ->
            localRepository.getWordsByCategories(categoryList, true)
                .cachedIn(viewModelScope).collectLatest {
                    send(it)
                }
        }
        awaitClose { cancel() }
    }

    suspend fun getWordWithInfoForFlashcards(): List<WordWithInfo> {
        return localRepository.getWordWithInfoForFlashcards(
            selectedCategories.value
        )
    }

    fun addCategory(context: Context, categoryWithInfo: CategoryWithInfo) {
        viewModelScope.launch {
            val resultId = localRepository.addCategory(categoryWithInfo)
            if (resultId == -1L) {
                context.showToast("You already have a category with this name")
            }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            localRepository.deleteCategory(category)
        }
    }

    fun updateRemember(word: Word) {
        viewModelScope.launch {
            localRepository.updateIncreaseRememberCount(word)
        }
    }

    fun updateForgot(word: Word) {
        viewModelScope.launch {
            localRepository.updateDecreaseRememberCount(word)
        }
    }

    fun updateShowForgotWord(word: Word) {
        viewModelScope.launch {
            localRepository.updateShowForgotWord(word)
        }
    }

    fun updateHideForgotWord(word: Word) {
        viewModelScope.launch {
            localRepository.updateHideForgotWord(word)
        }
    }

    fun updateResetAllForgotCount() {
        viewModelScope.launch {
            localRepository.updateResetAllForgotCount()
        }
    }

}