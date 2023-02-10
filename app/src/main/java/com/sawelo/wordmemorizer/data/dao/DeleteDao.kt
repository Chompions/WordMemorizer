package com.sawelo.wordmemorizer.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import com.sawelo.wordmemorizer.data.data_class.entity.Category
import com.sawelo.wordmemorizer.data.data_class.entity.Word
import com.sawelo.wordmemorizer.data.data_class.entity_cross_ref.WordCategoryMap

@Dao
interface DeleteDao {
    @Delete
    suspend fun deleteWord(word: Word)

    @Delete
    suspend fun deleteWordCategoryMap(wordCategoryMap: WordCategoryMap)

    @Query("UPDATE wordInfo SET rememberCount = 0")
    suspend fun deleteRememberCount()

    @Delete
    suspend fun deleteCategory(categoryWithInfo: Category)
}