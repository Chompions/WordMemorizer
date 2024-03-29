package com.sawelo.wordmemorizer.data.dao

import androidx.paging.PagingSource
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.sawelo.wordmemorizer.data.data_class.entity.Category
import com.sawelo.wordmemorizer.data.data_class.entity.Word
import com.sawelo.wordmemorizer.data.data_class.entity.WordInfo
import com.sawelo.wordmemorizer.data.data_class.relation_ref.CategoryWithInfo
import com.sawelo.wordmemorizer.data.data_class.relation_ref.WordWithCategories
import com.sawelo.wordmemorizer.data.data_class.relation_ref.WordWithInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface QueryDao {

    @Query("SELECT * FROM category ORDER BY categoryId")
    fun getCategories(): Flow<List<Category>>

    @Transaction
    @Query("SELECT * FROM category ORDER BY categoryId")
    fun getCategoriesWithInfo(): Flow<List<CategoryWithInfo>>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @RawQuery(observedEntities = [Word::class, WordInfo::class])
    fun getWordsPagingData(query: SupportSQLiteQuery): PagingSource<Int, WordWithInfo>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        "SELECT * FROM word " +
                "LEFT JOIN WordCategoryMap ON WordCategoryMap.wordIdMap = wordId " +
                "LEFT JOIN category ON category.categoryId = WordCategoryMap.categoryIdMap " +
                "WHERE wordId = :wordId"
    )
    suspend fun getWordWithCategoriesById(wordId: Int): WordWithCategories?

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        "SELECT DISTINCT * FROM word " +
                "LEFT JOIN WordInfo ON WordInfo.wordId = Word.wordId " +
                "LEFT JOIN WordCategoryMap ON WordCategoryMap.wordIdMap = Word.wordId " +
                "WHERE categoryIdMap IN (:categoryIdList) " +
                "GROUP BY Word.wordId " +
                "ORDER BY rememberCount DESC " +
                "LIMIT 20"
    )
    suspend fun getWordWithInfoForFlashcards(categoryIdList: List<Int>): List<WordWithInfo>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        "SELECT DISTINCT * FROM word WHERE " +
                "(wordText LIKE '%' || :wordInput || '%' AND :wordInput != '') OR " +
                "(furiganaText LIKE '%' || :furiganaInput || '%' AND :furiganaInput != '') OR " +
                "(definitionText LIKE '%' || :definitionInput || '%' AND :definitionInput != '')"
    )
    suspend fun getWordsWithInfoByText(
        wordInput: String,
        furiganaInput: String,
        definitionInput: String
    ): List<WordWithInfo>

    @Query("SELECT COUNT(wordId) FROM word")
    suspend fun getWordCountByAll(): Int

    @Query(
        "SELECT COUNT(wordId) FROM word " +
                "LEFT JOIN WordCategoryMap ON WordCategoryMap.wordIdMap = wordId " +
                "LEFT JOIN category ON category.categoryId = WordCategoryMap.categoryIdMap " +
                "WHERE categoryId = :categoryId"
    )
    suspend fun getWordCountByCategoryId(categoryId: Int): Int
}