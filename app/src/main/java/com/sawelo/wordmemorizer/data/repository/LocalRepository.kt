package com.sawelo.wordmemorizer.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.sawelo.wordmemorizer.data.data_class.entity.Category
import com.sawelo.wordmemorizer.data.data_class.entity.Word
import com.sawelo.wordmemorizer.data.data_class.entity_cross_ref.WordCategoryMap
import com.sawelo.wordmemorizer.data.data_class.relation_ref.CategoryWithInfo
import com.sawelo.wordmemorizer.data.data_class.relation_ref.WordWithCategories
import com.sawelo.wordmemorizer.data.data_class.relation_ref.WordWithInfo
import com.sawelo.wordmemorizer.data.database.AppDatabase
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

class LocalRepository(
    private val database: AppDatabase,
    private val preferenceRepository: PreferenceRepository,
) {

    fun getAllCategory(): Flow<List<Category>> =
        database.queryDao().getCategories()

    fun getAllCategoryWithInfo(): Flow<List<CategoryWithInfo>> =
        database.queryDao().getCategoriesWithInfo()

    fun getWordsByCategories(
        categoryList: List<Category>,
        getForgotten: Boolean,
    ): Flow<PagingData<WordWithInfo>> = callbackFlow {
        preferenceRepository.getCurrentSQLSortingStringFlow().collectLatest { sortingString ->
            val categoryIdList = categoryList.map { it.categoryId }

            val query = if (!getForgotten) {
                SimpleSQLiteQuery(
                    "SELECT DISTINCT * FROM Word " +
                            "LEFT JOIN WordInfo ON WordInfo.wordId = Word.wordId " +
                            "LEFT JOIN WordCategoryMap ON WordCategoryMap.wordIdMap = Word.wordId " +
                            "WHERE categoryIdMap IN (${categoryIdList.joinToString(",")}) " +
                            "GROUP BY Word.wordId " +
                            "ORDER BY $sortingString",
                )
            } else {
                SimpleSQLiteQuery(
                    "SELECT DISTINCT * FROM Word " +
                            "LEFT JOIN WordInfo ON WordInfo.wordId = Word.wordId " +
                            "LEFT JOIN WordCategoryMap ON WordCategoryMap.wordIdMap = Word.wordId " +
                            "WHERE categoryIdMap IN (${categoryIdList.joinToString(",")}) " +
                            "AND isForgotten = 1 " +
                            "GROUP BY Word.wordId " +
                            "ORDER BY $sortingString",
                )
            }

            Pager(
                PagingConfig(pageSize = 20)
            ) {
                database.queryDao().getWordsPagingData(query)
            }.flow.cancellable().collectLatest {
                send(it)
            }
        }
        awaitClose { cancel() }
    }

    suspend fun getWordWithCategoriesById(wordId: Int): WordWithCategories {
        return database.queryDao().getWordWithCategoriesById(wordId)
            ?: throw Exception("This word doesn't exist")
    }

    suspend fun getWordWithInfoForFlashcards(categoryList: List<Category>): List<WordWithInfo> {
        return database.queryDao().getWordWithInfoForFlashcards(categoryList.map { it.categoryId })
    }

    suspend fun getAllWordsByText(word: Word): List<WordWithInfo> {
        return database.queryDao().getWordsWithInfoByText(
            wordInput = word.wordText.filter { it.isLetter() && !it.isWhitespace() },
            furiganaInput = word.furiganaText.filter { it.isLetter() && !it.isWhitespace() },
            definitionInput = word.definitionText.filter { it.isLetter() && !it.isWhitespace() }
        )
    }

    suspend fun addWordWithCategories(wordWithCategories: WordWithCategories) {
        database.insertDao().insertWordWithCategories(wordWithCategories)
        checkAndUpdateWordCount()
    }

    suspend fun addCategory(categoryWithInfo: CategoryWithInfo): Long {
        return database.insertDao().insertCategoryWithInfo(categoryWithInfo)
    }

    suspend fun updateWordWithCategories(
        oldData: WordWithCategories,
        newData: WordWithCategories
    ) {
        database.updateDao().updateWord(newData.wordWithInfo.word)
        database.updateDao().updateWordInfo(newData.wordWithInfo.wordInfo)
        oldData.categories.forEach { category ->
            database.deleteDao().deleteWordCategoryMap(
                WordCategoryMap(
                    oldData.wordWithInfo.word.wordId,
                    category.categoryId
                )
            )
        }
        newData.categories.forEach { category ->
            database.insertDao().insertWordCategoryMap(
                WordCategoryMap(
                    newData.wordWithInfo.word.wordId,
                    category.categoryId
                )
            )
        }
        checkAndUpdateWordCount()
    }

    suspend fun updateIncreaseRememberCount(word: Word) {
        database.updateDao().updateIncreaseRememberCountById(word.wordId)
    }

    suspend fun updateDecreaseRememberCount(word: Word) {
        database.updateDao().updateDecreaseRememberCountById(word.wordId)
    }

    suspend fun updateShowForgotWord(word: Word) {
        database.updateDao().updateShowForgotWord(word.wordId)
    }

    suspend fun updateHideForgotWord(word: Word) {
        database.updateDao().updateHideForgotWord(word.wordId)
    }

    suspend fun updateResetAllForgotCount() {
        database.deleteDao().deleteRememberCount()
    }

    suspend fun deleteWord(word: Word) {
        database.deleteDao().deleteWord(word)
        checkAndUpdateWordCount()
    }

    suspend fun deleteCategory(category: Category) {
        database.deleteDao().deleteCategory(category)
    }

    private suspend fun checkAndUpdateWordCount() {
        getAllCategoryWithInfo().first().forEach { categoryWithInfo ->
            val id = categoryWithInfo.category.categoryId
            database.queryDao().getWordCountByCategoryId(id).also { wordCount ->
                database.updateDao().updateWordCountCategory(id, wordCount)
            }
        }
    }
}