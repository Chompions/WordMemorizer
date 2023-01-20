package com.sawelo.wordmemorizer.data.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.sawelo.wordmemorizer.data.data_class.Word
import com.sawelo.wordmemorizer.data.data_class.WordCategoryMap
import com.sawelo.wordmemorizer.data.data_class.WordWithCategories

@Dao
interface WordDao {

    @Query(
        "SELECT DISTINCT * FROM word " +
                "ORDER BY :sortingString"
    )
    fun getAllWordsPagingData(sortingString: String): PagingSource<Int, Word>

    @Transaction
    @Query(
        "SELECT DISTINCT * FROM word " +
                "JOIN WordCategoryMap ON WordCategoryMap.wordIdMap = wordId " +
                "JOIN category ON category.categoryId = WordCategoryMap.categoryIdMap " +
                "WHERE categoryId = :inputCategoryId " +
                "ORDER BY :sortingString"
    )
    fun getAllWordsPagingDataByCategory(
        inputCategoryId: Int, sortingString: String
    ): PagingSource<Int, Word>

    @Transaction
    @Query(
        "SELECT DISTINCT * FROM word " +
                "WHERE isForgotten = 1"
    )
    fun getForgottenWordsPagingData(): PagingSource<Int, Word>

    @Transaction
    @Query(
        "SELECT DISTINCT * FROM word " +
                "JOIN WordCategoryMap ON WordCategoryMap.wordIdMap = wordId " +
                "JOIN category ON category.categoryId = WordCategoryMap.categoryIdMap " +
                "WHERE categoryId = :inputCategoryId AND isForgotten = 1"
    )
    fun getForgottenWordsByCategoryPagingData(
        inputCategoryId: Int
    ): PagingSource<Int, Word>

    @Query(
        "SELECT DISTINCT * FROM word WHERE " +
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

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertWord(word: Word): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertWordCategoryMap(wordCategoryMap: WordCategoryMap)

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

    @Query("UPDATE word SET forgotCount = 0")
    suspend fun deleteForgotCount()

    @Update
    suspend fun updateWord(word: Word)

    @Delete
    suspend fun deleteWord(word: Word)
}