package com.sawelo.wordmemorizer.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.sawelo.wordmemorizer.data.converter.CategoryConverter
import com.sawelo.wordmemorizer.data.data_class.Category
import com.sawelo.wordmemorizer.data.data_class.Word
import com.sawelo.wordmemorizer.util.PreferencesUtils
import com.sawelo.wordmemorizer.util.SortingAnchor
import com.sawelo.wordmemorizer.util.SortingOrder
import com.sawelo.wordmemorizer.util.WordUtils.isAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collectLatest

class WordRepository(
    private val database: AppDatabase,
    private val dataStore: DataStore<Preferences>,
) {

    fun getAllCategories(): Flow<List<Category>> = database.categoryDao().getCategories()

    fun getAllWordsPagingData(
        category: Category
    ): Flow<PagingData<Word>> = callbackFlow {
        dataStore.data.cancellable().collectLatest { preferences ->
            val sortingAnchorEnum = PreferencesUtils.obtainCurrentSortingFromPreferences<SortingAnchor>(preferences)
            val sortingOrderEnum = PreferencesUtils.obtainCurrentSortingFromPreferences<SortingOrder>(preferences)

            val getAllWordsQuery = SimpleSQLiteQuery(
                "SELECT * FROM word ORDER BY " +
                        "${sortingAnchorEnum.obtainQueryString()} ${sortingOrderEnum.obtainQueryString()}"
            )
            val categoryQueryString = CategoryConverter()
                .fromCategoryToJson(category.copy(wordCount = 0))
            val getAllWordsByCategoryQuery = SimpleSQLiteQuery(
                "SELECT * FROM word WHERE categoryList " +
                        "LIKE '%' || '$categoryQueryString' || '%' ORDER BY " +
                        "${sortingAnchorEnum.obtainQueryString()} ${sortingOrderEnum.obtainQueryString()}"
            )

            Pager(
                PagingConfig(pageSize = 20)
            ) {
                if (category.isAll()) {
                    database.wordDao().getAllWordsPagingData(getAllWordsQuery)
                } else {
                    database.wordDao().getAllWordsPagingData(getAllWordsByCategoryQuery)
                }
            }.flow.cancellable().collectLatest {
                send(it)
            }
        }
        awaitClose { cancel() }
    }

    fun getAllForgottenWordsPagingData(
        category: Category
    ): Flow<PagingData<Word>> = callbackFlow {
        Pager(
            PagingConfig(pageSize = 20)
        ) {
            if (category.isAll()) {
                database.wordDao().getForgottenWordsPagingData()
            } else {
                database.wordDao().getForgottenWordsByCategoryPagingData(category.copy(wordCount = 0))
            }
        }.flow.cancellable().collectLatest {
            send(it)
        }
        awaitClose { cancel() }
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