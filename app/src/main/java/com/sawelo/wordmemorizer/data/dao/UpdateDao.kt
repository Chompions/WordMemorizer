@file:Suppress("FunctionName")

package com.sawelo.wordmemorizer.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.sawelo.wordmemorizer.data.data_class.entity.Word

@Dao
interface UpdateDao {

    @Query ("UPDATE categoryInfo SET wordCount = :wordCount WHERE categoryId = :categoryId")
    suspend fun updateWordCountCategory(categoryId: Int, wordCount: Int)

    @Transaction
    suspend fun updateShowForgotWord(wordId: Int) {
        _updateIsForgottenById(wordId, true)
        _updateForgotCountById(wordId)
    }

    @Transaction
    suspend fun updateHideForgotWord(wordId: Int) {
        _updateIsForgottenById(wordId, false)
    }

    @Query("UPDATE wordInfo SET isForgotten = :isForgotten WHERE wordId = :id")
    fun _updateIsForgottenById(id: Int, isForgotten: Boolean)

    @Query("UPDATE wordInfo SET rememberCount = rememberCount - :decrement WHERE wordId = :id")
    fun _updateForgotCountById(id: Int, decrement: Int = 1)

    @Update
    fun updateWord(word: Word)
}