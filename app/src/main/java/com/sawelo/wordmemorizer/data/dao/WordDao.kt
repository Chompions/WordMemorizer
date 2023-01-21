package com.sawelo.wordmemorizer.data.dao

import androidx.paging.PagingSource
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.sawelo.wordmemorizer.data.data_class.Word
import com.sawelo.wordmemorizer.data.data_class.WordCategoryMap
import com.sawelo.wordmemorizer.data.data_class.WordWithCategories

@Dao
interface WordDao {

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @RawQuery(observedEntities = [Word::class])
    fun getWordsPagingData(query: SupportSQLiteQuery): PagingSource<Int, Word>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        "SELECT * FROM word " +
                "LEFT JOIN WordCategoryMap ON WordCategoryMap.wordIdMap = wordId " +
                "LEFT JOIN category ON category.categoryId = WordCategoryMap.categoryIdMap " +
                "WHERE wordId = :wordId"
    )
    suspend fun getWordWithCategoriesById(wordId: Int): WordWithCategories?

    @Query("SELECT COUNT(wordId) FROM word")
    suspend fun getWordCountByAll(): Int

    @Query(
        "SELECT COUNT(wordId) FROM word " +
                "LEFT JOIN WordCategoryMap ON WordCategoryMap.wordIdMap = wordId " +
                "LEFT JOIN category ON category.categoryId = WordCategoryMap.categoryIdMap " +
                "WHERE categoryId = :categoryId"
    )
    suspend fun getWordCountByCategoryId(categoryId: Int): Int

    @Transaction
    @Query(
        "SELECT * FROM word " +
                "WHERE isForgotten = 1"
    )
    fun getForgottenWordsPagingData(): PagingSource<Int, Word>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        "SELECT * FROM word " +
                "JOIN WordCategoryMap ON WordCategoryMap.wordIdMap = wordId " +
                "JOIN category ON category.categoryId = WordCategoryMap.categoryIdMap " +
                "WHERE categoryId = :inputCategoryId AND isForgotten = 1"
    )
    fun getForgottenWordsByCategoryPagingData(
        inputCategoryId: Int
    ): PagingSource<Int, Word>

    @Query(
        "SELECT * FROM word WHERE " +
                "(wordText LIKE '%' || :wordText || '%' AND :wordText != '') OR " +
                "(furiganaText LIKE '%' || :furiganaText || '%' AND :furiganaText != '') OR " +
                "(definitionText LIKE '%' || :definitionText || '%' AND :definitionText != '')"
    )
    suspend fun getAllWordsByText(
        wordText: String,
        furiganaText: String,
        definitionText: String
    ): List<Word>

    /**
     * Function for inserting word combined with WordCategoryMap
     */
    @Transaction
    suspend fun insertWordWithCategories(wordWithCategories: WordWithCategories) {
        val insertedWordId = insertWord(wordWithCategories.word).toInt()
        wordWithCategories.categories.forEach { category ->
            insertWordCategoryMap(
                WordCategoryMap(insertedWordId, category.categoryId)
            )
        }
    }

    /**
     * Function for updating word combined with WordCategoryMap
     */
    @Transaction
    suspend fun updateWordWithCategories(
        oldData: WordWithCategories,
        newData: WordWithCategories
    ) {
        updateWord(newData.word)
        oldData.categories.forEach { category ->
            deleteWordCategoryMap(
                WordCategoryMap(oldData.word.wordId, category.categoryId)
            )
        }
        newData.categories.forEach { category ->
            insertWordCategoryMap(
                WordCategoryMap(newData.word.wordId, category.categoryId)
            )
        }
    }
    @Delete
    suspend fun deleteWord(word: Word)
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertWord(word: Word): Long
    @Update
    fun updateWord(word: Word)
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertWordCategoryMap(wordCategoryMap: WordCategoryMap)
    @Delete
    fun deleteWordCategoryMap(wordCategoryMap: WordCategoryMap)


    /**
     * Function for showing/hiding forgotten words
     */
    @Transaction
    suspend fun updateShowForgotWord(wordId: Int) {
        updateIsForgottenById(wordId, true)
        updateForgotCountById(wordId)
    }

    @Transaction
    suspend fun updateHideForgotWord(wordId: Int) {
        updateIsForgottenById(wordId, false)
    }

    @Query("UPDATE word SET isForgotten = :isForgotten WHERE wordId = :id")
    fun updateIsForgottenById(id: Int, isForgotten: Boolean)

    @Query("UPDATE word SET forgotCount = forgotCount + :incrementByInt WHERE wordId = :id")
    fun updateForgotCountById(id: Int, incrementByInt: Int = 1)

    /**
     * Miscellaneous functions
     */
    @Query("UPDATE word SET forgotCount = 0")
    suspend fun deleteForgotCount()
}