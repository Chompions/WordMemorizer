package com.sawelo.wordmemorizer.data

import androidx.paging.PagingSource
import androidx.room.*

@Dao
interface WordDao {
//    @Query ("SELECT * FROM word ORDER BY createdTimeMillis DESC")
//    fun getWords(): Flow<List<Word>>

    @Query ("SELECT * FROM word ORDER BY RANDOM()")
    fun getAllWordsPagingData(): PagingSource<Int, Word>

    @Query ("SELECT * FROM word WHERE categoryList LIKE '%' || :category || '%' ORDER BY RANDOM()")
    fun getAllWordsByCategoryPagingData(category: Category): PagingSource<Int, Word>

    @Query ("SELECT * FROM word WHERE isForgotten = 1")
    fun getForgottenWordsPagingData(): PagingSource<Int, Word>

    @Query ("SELECT COUNT(id) FROM word WHERE categoryList LIKE '%' || :category || '%'")
    suspend fun getAllWordsSizeByCategory(category: Category): Int

    @Query ("SELECT * FROM word WHERE wordText LIKE '%' || :wordText || '%'")
    suspend fun getWordsByWord(wordText: String): List<Word>


//    @Query ("SELECT * FROM word WHERE categoryList LIKE '%' || :category || '%'")
//    fun getWordsByCategory(category: Category): Flow<List<Word>>
//
//    @Query ("SELECT * FROM word ORDER BY forgotCount DESC LIMIT :limit")
//    suspend fun getWordsByLimit(limit: Int): List<Word>
//
//

    @Query ("UPDATE word SET isForgotten = :isForgotten WHERE id = :id")
    suspend fun updateIsForgottenById(id: Int, isForgotten: Boolean)

    @Query ("UPDATE word SET forgotCount = forgotCount + :incrementByInt WHERE id = :id")
    suspend fun updateForgotCountById(id: Int, incrementByInt: Int = 1)

    @Query ("UPDATE word SET forgotCount = 0")
    suspend fun deleteForgotCount()

    @Insert (onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWord(word: Word): Long

    @Delete
    suspend fun deleteWord(word: Word)
}