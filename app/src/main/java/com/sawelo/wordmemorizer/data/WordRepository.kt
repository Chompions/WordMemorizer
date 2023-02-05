package com.sawelo.wordmemorizer.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.sawelo.wordmemorizer.data.data_class.Category
import com.sawelo.wordmemorizer.data.data_class.JishoResponse
import com.sawelo.wordmemorizer.data.data_class.Word
import com.sawelo.wordmemorizer.data.data_class.WordWithCategories
import com.sawelo.wordmemorizer.data.remote.JishoService
import com.sawelo.wordmemorizer.util.PreferencesUtils
import com.sawelo.wordmemorizer.util.WordUtils.isAll
import com.sawelo.wordmemorizer.util.sorting_utils.SortingAnchor
import com.sawelo.wordmemorizer.util.sorting_utils.SortingOrder
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

class WordRepository(
    private val database: AppDatabase,
    private val dataStore: DataStore<Preferences>,
    private val jishoService: JishoService,
) {

    suspend fun searchWordFromJisho(word: String): JishoResponse? {
        return jishoService.searchWord(word)
    }

    fun getAllWordsPagingData(
        category: Category
    ): Flow<PagingData<Word>> = callbackFlow {
        checkAndUpdateWordCount()
        dataStore.data.cancellable().collectLatest { preferences ->
            val sortingAnchorEnum =
                PreferencesUtils.obtainCurrentSortingFromPreferences<SortingAnchor>(preferences)
            val sortingOrderEnum =
                PreferencesUtils.obtainCurrentSortingFromPreferences<SortingOrder>(preferences)
            val sortingString =
                "${sortingAnchorEnum.obtainQueryString()} ${sortingOrderEnum.obtainQueryString()}"

            val allWordsQuery = SimpleSQLiteQuery(
                "SELECT DISTINCT * FROM word " +
                        "ORDER BY $sortingString"
            )

            val categoryWordsQuery = SimpleSQLiteQuery(
                "SELECT DISTINCT * FROM word " +
                        "JOIN WordCategoryMap ON WordCategoryMap.wordIdMap = wordId " +
                        "JOIN category ON category.categoryId = WordCategoryMap.categoryIdMap " +
                        "WHERE categoryId = ${category.categoryId} " +
                        "ORDER BY $sortingString"
            )

            Pager(
                PagingConfig(pageSize = 20)
            ) {
                if (category.isAll()) {
                    database.wordDao().getWordsPagingData(allWordsQuery)
                } else {
                    database.wordDao().getWordsPagingData(categoryWordsQuery)
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

    suspend fun getWordWithCategoriesById(wordId: Int): WordWithCategories {
        return database.wordDao().getWordWithCategoriesById(wordId)
            ?: throw Exception("This word doesn't exist")
    }

    suspend fun getAllWordsByText(
        wordText: String,
        furiganaText: String,
        definitionText: String,
    ): List<Word> {
        return database.wordDao().getAllWordsByText(
            wordText.filter { it.isLetter() && !it.isWhitespace() },
            furiganaText.filter { it.isLetter() && !it.isWhitespace() },
            definitionText.filter { it.isLetter() && !it.isWhitespace() }
        )
    }

    suspend fun addWordWithCategories(wordWithCategories: WordWithCategories) {
        database.wordDao().insertWordWithCategories(wordWithCategories)
        checkAndUpdateWordCount()
    }

    suspend fun updateWordWithCategories(
        oldData: WordWithCategories,
        newData: WordWithCategories
    ) {
        database.wordDao().updateWordWithCategories(oldData, newData)
        checkAndUpdateWordCount()
    }

    suspend fun deleteWord(word: Word) {
        database.wordDao().deleteWord(word)
        checkAndUpdateWordCount()
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

    fun getAllCategories(): Flow<List<Category>> = database.categoryDao().getCategoryList()

    suspend fun addCategory(category: Category): Long {
        return database.categoryDao().insertCategory(category)
    }

    suspend fun deleteCategory(category: Category) {
        database.categoryDao().deleteCategory(category)
    }

    private suspend fun checkAndUpdateWordCount() {
        getAllCategories().first().forEach { category ->
            if (category.isAll()) {
                database.wordDao().getWordCountByAll().also { wordCount ->
                    database.categoryDao().setWordCountById(1, wordCount)
                }
            } else {
                database.wordDao().getWordCountByCategoryId(category.categoryId).also { wordCount ->
                    database.categoryDao().setWordCountById(category.categoryId, wordCount)
                }
            }
        }
    }
}