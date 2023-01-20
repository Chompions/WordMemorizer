package com.sawelo.wordmemorizer.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.sawelo.wordmemorizer.data.data_class.Category
import com.sawelo.wordmemorizer.data.data_class.Word
import com.sawelo.wordmemorizer.data.data_class.WordWithCategories
import com.sawelo.wordmemorizer.util.PreferencesUtil
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

    fun getAllWordsPagingData(
        category: Category
    ): Flow<PagingData<Word>> = callbackFlow {
        dataStore.data.cancellable().collectLatest { preferences ->
            val sortingAnchorEnum =
                PreferencesUtil.obtainCurrentSortingFromPreferences<SortingAnchor>(preferences)
            val sortingOrderEnum =
                PreferencesUtil.obtainCurrentSortingFromPreferences<SortingOrder>(preferences)
            val sortingString =
                "${sortingAnchorEnum.obtainQueryString()} ${sortingOrderEnum.obtainQueryString()}"

            Pager(
                PagingConfig(pageSize = 20)
            ) {
                if (category.isAll()) {
                    database.wordDao().getAllWordsPagingData(sortingString)
                } else {
                    database.wordDao()
                        .getAllWordsPagingDataByCategory(category.categoryId, sortingString)
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
                database.wordDao()
                    .getForgottenWordsByCategoryPagingData(category.categoryId)
            }
        }.flow.cancellable().collectLatest {
            send(it)
        }
        awaitClose { cancel() }
    }

    suspend fun getAllWordsByText(
        wordText: String,
        furiganaText: String,
        definitionText: String,
    ): List<Word> {
        return database.wordDao().getAllWordsByText(
            wordText.filter { it.isLetter() && !it.isWhitespace()},
            furiganaText.filter { it.isLetter() && !it.isWhitespace()},
            definitionText.filter { it.isLetter() && !it.isWhitespace()}
        )
    }

    suspend fun addWord(wordWithCategories: WordWithCategories) {
        database.wordDao().insertWordWithCategories(wordWithCategories)
        database.categoryDao().incrementWordCountById(1)
        wordWithCategories.categories.forEach {
            database.categoryDao().incrementWordCountById(it.categoryId)
        }
    }

    suspend fun deleteWord(wordWithCategories: WordWithCategories) {
        database.wordDao().deleteWord(wordWithCategories.word)
        database.categoryDao().decrementWordCountById(1)
        wordWithCategories.categories.forEach {
            database.categoryDao().decrementWordCountById(it.categoryId)
        }
    }

    suspend fun updateShowForgotWord(word: Word) {
        database.wordDao().updateShowForgotWord(word.wordId)
    }

    suspend fun updateHideForgotWord(word: Word) {
        database.wordDao().updateHideForgotWord(word.wordId)
    }

    suspend fun resetAllForgotCount() {
        database.wordDao().deleteForgotCount()
    }

    fun getAllCategories(): Flow<List<Category>> = database.categoryDao().getCategoriesName()

    suspend fun addCategory(category: Category) {
        database.categoryDao().insertCategory(category)
    }

    suspend fun deleteCategory(category: Category) {
        database.categoryDao().deleteCategory(category)
    }

//
//    suspend fun updateWord(word: Word, isIncreaseCount: Boolean) {
//        database.wordDao().updateWord(word.withoutCategoryWordCount())
//        if (isIncreaseCount) {
//            increaseForgotCount(word.withoutCategoryWordCount())
//        } else decreaseForgotCount(word.withoutCategoryWordCount())
//    }

}