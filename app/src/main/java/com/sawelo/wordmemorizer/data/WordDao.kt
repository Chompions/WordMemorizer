package com.sawelo.wordmemorizer.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Query ("SELECT * FROM word ORDER BY forgotCount DESC")
    fun getWords(): Flow<List<Word>>

    @Query ("SELECT * FROM word ORDER BY forgotCount DESC LIMIT :limit")
    suspend fun getWordsByLimit(limit: Int): List<Word>

    @Query ("SELECT * FROM word WHERE kanjiText LIKE '%' || :kanji || '%'")
    fun getWordsByKanji(kanji: String): Flow<List<Word>>

    @Query ("UPDATE word SET forgotCount = forgotCount + 1 WHERE id = :id")
    suspend fun updateForgotCountById(id: Int)

    @Query ("UPDATE word SET forgotCount = 0")
    suspend fun deleteForgotCount()

    @Insert (onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWord(word: Word)

    @Delete
    suspend fun deleteWord(word: Word)
}