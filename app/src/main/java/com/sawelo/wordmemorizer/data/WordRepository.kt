package com.sawelo.wordmemorizer.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class WordRepository(private val database: AppDatabase) {

    fun getAllWordsPagingData(id: Int? = null): Flow<PagingData<Word>> {
        return Pager(
            PagingConfig(pageSize = 20)
        ) {
            database.wordDao().getWordsPagingData(id)
        }.flow
    }

    private val allWords = database.wordDao().getWords()
    private val allCategories = database.categoryDao().getCategories()

    fun getAllCategories(): Flow<List<Category>> = allCategories
    fun getAllWordsByAll(): Flow<List<Word>> = allWords
    fun getAllWordsByCategory(category: Category): Flow<List<Word>> =
        allWords.mapLatest { words ->
            words.filter { word -> category in word.categoryList }
        }
    fun getAllWordsByWord(inputString: String): Flow<List<Word>> =
        allWords.mapLatest { words ->
            words.filter { word -> if (inputString.isNotBlank())
                inputString in word.wordText else false }
        }

    suspend fun addWord(word: Word, callback: (id: Int) -> Unit) {
        val id = database.wordDao().insertWord(word)
        allWords.collectLatest { callback.invoke(id.toInt()) }
    }

    suspend fun addCategory(category: Category, callback: (id: Int) -> Unit) {
        val id = database.categoryDao().insertCategory(category)
        allCategories.collectLatest { callback.invoke(id.toInt()) }
    }

    suspend fun updateForgotCountWord(word: Word, callback: () -> Unit) {
        database.wordDao().updateForgotCountById(word.id)
        allWords.collectLatest { callback.invoke() }
    }

    suspend fun updateIsForgottenWord(word: Word, isForgotten: Boolean, callback: () -> Unit) {
        database.wordDao().updateIsForgottenById(word.id, isForgotten)
        allWords.collectLatest { callback.invoke() }
    }

    suspend fun resetAllForgotCount(callback: () -> Unit) {
        database.wordDao().deleteForgotCount()
        allWords.collectLatest { callback.invoke() }
    }

    suspend fun deleteWord(word: Word, callback: () -> Unit) {
        database.wordDao().deleteWord(word)
        allWords.collectLatest { callback.invoke() }
    }

    suspend fun deleteCategory(category: Category, callback: () -> Unit) {
        database.categoryDao().deleteCategory(category)
        allWords.collectLatest { callback.invoke() }
    }
}