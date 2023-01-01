package com.sawelo.wordmemorizer.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.sawelo.wordmemorizer.data.data_class.Category
import com.sawelo.wordmemorizer.data.data_class.Word
import kotlinx.coroutines.flow.Flow

class WordRepository(private val database: AppDatabase) {

    fun getAllCategories(): Flow<List<Category>> = database.categoryDao().getCategories()

    fun getAllWordsPagingData(): Flow<PagingData<Word>> {
        return Pager(
            PagingConfig(pageSize = 20)
        ) {
            database.wordDao().getAllWordsPagingData()
        }.flow
    }

    fun getAllWordsByCategoryPagingData(category: Category): Flow<PagingData<Word>> {
        return Pager(
            PagingConfig(pageSize = 20)
        ) {
            database.wordDao().getAllWordsByCategoryPagingData(category.copy(wordCount = 0))
        }.flow
    }

    fun getAllForgottenWordsPagingData(): Flow<PagingData<Word>> {
        return Pager(
            PagingConfig(pageSize = 20)
        ) {
            database.wordDao().getForgottenWordsPagingData()
        }.flow
    }

    fun getAllForgottenWordByCategoryPagingData(category: Category): Flow<PagingData<Word>> {
        return Pager(
            PagingConfig(pageSize = 20)
        ) {
            database.wordDao().getForgottenWordsByCategoryPagingData(category.copy(wordCount = 0))
        }.flow
    }

    suspend fun getAllWordsByWord(wordText: String): List<Word> {
        return database.wordDao().getWordsByWord(wordText)
    }

    suspend fun addWord(word: Word) {
        database.wordDao().insertWord(word)
        // Increase wordCount for 'All' category
        database.categoryDao().updateWordCountById(1, 1)
        // Increase wordCount for every chosen category
        word.categoryList.forEach {
            database.categoryDao().updateWordCountById(it.id, 1)
        }
    }

    suspend fun addCategory(category: Category) {
        database.categoryDao().insertCategory(category)
    }

    suspend fun updateForgotCountWord(word: Word, incrementByInt: Int = 1) {
        database.wordDao().updateForgotCountById(word.id, incrementByInt)
    }

    suspend fun updateIsForgottenWord(word: Word, isForgotten: Boolean) {
        database.wordDao().updateIsForgottenById(word.id, isForgotten)
    }

    suspend fun resetAllForgotCount() {
        database.wordDao().deleteForgotCount()
    }

    suspend fun deleteWord(word: Word) {
        database.wordDao().deleteWord(word)
        // Decrease wordCount for 'All' category
        database.categoryDao().updateWordCountById(1, -1)
        // Decrease wordCount for every chosen category
        word.categoryList.forEach {
            database.categoryDao().updateWordCountById(it.id, -1)
        }
    }

    suspend fun deleteCategory(category: Category) {
        database.categoryDao().deleteCategory(category)
    }
}