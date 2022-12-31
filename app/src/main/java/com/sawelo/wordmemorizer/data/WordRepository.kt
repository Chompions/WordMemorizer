package com.sawelo.wordmemorizer.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

class WordRepository(private val database: AppDatabase) {

    fun getAllCategories(): Flow<List<Category>> = database.categoryDao().getCategories()

    fun getAllWordsByCategoryPagingData(category: Category): Flow<PagingData<Word>> {
        return Pager(
            PagingConfig(pageSize = 20)
        ) {
            database.wordDao().getAllWordsByCategoryPagingData(category)
        }.flow
    }

    fun getAllWordsPagingData(): Flow<PagingData<Word>> {
        return Pager(
            PagingConfig(pageSize = 20)
        ) {
            database.wordDao().getAllWordsPagingData()
        }.flow
    }

    fun getAllForgottenWordsPagingData(): Flow<PagingData<Word>> {
        return Pager(
            PagingConfig(pageSize = 20)
        ) {
            database.wordDao().getForgottenWordsPagingData()
        }.flow
    }

    suspend fun getAllWordsSizeByCategory(category: Category): Int {
        return database.wordDao().getAllWordsSizeByCategory(category)
    }

    suspend fun getAllWordsByWord(wordText: String): List<Word> {
        return database.wordDao().getWordsByWord(wordText)
    }

    suspend fun addWord(word: Word) {
        database.wordDao().insertWord(word)
        // Add wordCount for 'All' category
        database.categoryDao().updateWordCountById(1, 1)
        // Add wordCount for every chosen category
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
        word.categoryList.forEach {
            database.categoryDao().updateWordCountById(it.id, -1)
        }
    }

    suspend fun deleteCategory(category: Category) {
        database.categoryDao().deleteCategory(category)
    }



//    private val allWords = database.wordDao().getWords()
//    private val allCategories =
//
//    allCategories
//    fun getAllWordsByAll(): Flow<List<Word>> = allWords
//    fun getAllWordsByCategory(category: Category): Flow<List<Word>> =
//        allWords.mapLatest { words ->
//            words.filter { word -> category in word.categoryList }
//        }
//    fun getAllWordsByWord(inputString: String): Flow<List<Word>> =
//        allWords.mapLatest { words ->
//            words.filter { word -> if (inputString.isNotBlank())
//                inputString in word.wordText else false }
//        }


}