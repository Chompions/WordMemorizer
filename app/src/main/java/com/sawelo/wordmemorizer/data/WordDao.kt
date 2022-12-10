package com.sawelo.wordmemorizer.data

import androidx.room.*

@Dao
interface WordDao {
    @Query ("SELECT * FROM word ORDER BY forgotCount DESC")
    suspend fun getWords(): List<Word>

    @Query ("SELECT * FROM word ORDER BY forgotCount DESC LIMIT :limit")
    suspend fun getWordsByLimit(limit: Int): List<Word>

    @Query ("UPDATE word SET forgotCount = forgotCount + 1 WHERE kanjiText = :kanji")
    suspend fun updateForgotCountByKanji(kanji: String): Int

    @Insert (onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWord(word: Word): Long

    @Delete
    suspend fun deleteWord(word: Word): Int
}