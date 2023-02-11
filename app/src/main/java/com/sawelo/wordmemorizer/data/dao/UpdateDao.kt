@file:Suppress("FunctionName")

package com.sawelo.wordmemorizer.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.sawelo.wordmemorizer.data.data_class.entity.Word
import com.sawelo.wordmemorizer.data.data_class.entity.WordInfo

@Dao
interface UpdateDao {

    @Query ("UPDATE categoryInfo SET wordCount = :wordCount WHERE categoryId = :categoryId")
    suspend fun updateWordCountCategory(categoryId: Int, wordCount: Int)

    @Transaction
    suspend fun updateShowForgotWord(wordId: Int) {
        _updateIsForgottenById(wordId, true)
        updateDecreaseRememberCountById(wordId)
    }

    @Transaction
    suspend fun updateHideForgotWord(wordId: Int) {
        _updateIsForgottenById(wordId, false)
    }

    @Query("UPDATE wordInfo SET rememberCount = rememberCount - 1 WHERE wordId = :id")
    suspend fun updateDecreaseRememberCountById(id: Int)

    @Query("UPDATE wordInfo SET rememberCount = rememberCount + 1 WHERE wordId = :id")
    suspend fun updateIncreaseRememberCountById(id: Int)

    @Query("UPDATE wordInfo SET isForgotten = :isForgotten WHERE wordId = :id")
    suspend fun _updateIsForgottenById(id: Int, isForgotten: Boolean)

    @Update
    suspend fun updateWord(word: Word)

    @Update
    suspend fun updateWordInfo(wordInfo: WordInfo)
}