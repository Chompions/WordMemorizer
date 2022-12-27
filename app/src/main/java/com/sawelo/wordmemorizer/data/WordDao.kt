package com.sawelo.wordmemorizer.data

import androidx.paging.PagingData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Query ("SELECT * FROM word ORDER BY createdTimeMillis DESC")
    fun getWords(): Flow<List<Word>>

    @Query ("SELECT * FROM (SELECT * FROM word ORDER BY RANDOM()) " +
            "UNION SELECT * FROM (SELECT * FROM word WHERE (:id IS NULL OR id = :id))")
    fun getWordsPagingData(id: Int? = null): PagingData<List<Word>>

//    @Query ("SELECT * FROM word WHERE categoryList LIKE '%' || :category || '%'")
//    fun getWordsByCategory(category: Category): Flow<List<Word>>
//
//    @Query ("SELECT * FROM word ORDER BY forgotCount DESC LIMIT :limit")
//    suspend fun getWordsByLimit(limit: Int): List<Word>
//
//    @Query ("SELECT * FROM word WHERE wordText LIKE '%' || :kanji || '%'")
//    suspend fun getWordsByKanji(kanji: String): List<Word>

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