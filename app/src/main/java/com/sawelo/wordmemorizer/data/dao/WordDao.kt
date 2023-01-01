package com.sawelo.wordmemorizer.data.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.sawelo.wordmemorizer.data.data_class.Category
import com.sawelo.wordmemorizer.data.data_class.Word

@Dao
interface WordDao {
    @Query ("SELECT * FROM word ORDER BY forgotCount ASC")
    fun getAllWordsPagingData(): PagingSource<Int, Word>

    @Query ("SELECT * FROM word WHERE categoryList LIKE '%' || :category || '%' ORDER BY forgotCount ASC")
    fun getAllWordsByCategoryPagingData(category: Category): PagingSource<Int, Word>

    @Query ("SELECT * FROM word WHERE isForgotten = 1")
    fun getForgottenWordsPagingData(): PagingSource<Int, Word>

    @Query ("SELECT * FROM word WHERE isForgotten = 1 AND categoryList LIKE '%' || :category || '%'")
    fun getForgottenWordsByCategoryPagingData(category: Category): PagingSource<Int, Word>

    @Query ("SELECT * FROM word WHERE wordText LIKE '%' || :wordText || '%'")
    suspend fun getWordsByWord(wordText: String): List<Word>

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