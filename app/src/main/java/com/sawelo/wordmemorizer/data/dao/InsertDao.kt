@file:Suppress("FunctionName")

package com.sawelo.wordmemorizer.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import com.sawelo.wordmemorizer.data.data_class.entity.Category
import com.sawelo.wordmemorizer.data.data_class.entity.CategoryInfo
import com.sawelo.wordmemorizer.data.data_class.entity.Word
import com.sawelo.wordmemorizer.data.data_class.entity.WordInfo
import com.sawelo.wordmemorizer.data.data_class.entity_cross_ref.WordCategoryMap
import com.sawelo.wordmemorizer.data.data_class.relation_ref.CategoryWithInfo
import com.sawelo.wordmemorizer.data.data_class.relation_ref.WordWithCategories
import com.sawelo.wordmemorizer.data.data_class.relation_ref.WordWithInfo

@Dao
interface InsertDao {

    @Transaction
    suspend fun insertWordWithCategories(wordWithCategories: WordWithCategories) {
        _insertWordWithInfo(wordWithCategories.wordWithInfo).let { insertedId ->
            wordWithCategories.categories.forEach { category ->
                insertWordCategoryMap(
                    WordCategoryMap(insertedId.toInt(), category.categoryId)
                )
            }
        }
    }

    @Transaction
    suspend fun insertCategoryWithInfo(categoryWithInfo: CategoryWithInfo): Long {
        return _insertCategory(categoryWithInfo.category).let { insertedId ->
            _insertCategoryInfo(
                categoryWithInfo.categoryInfo.copy(categoryId = insertedId.toInt())
            )
            insertedId
        }
    }

    @Transaction
    suspend fun _insertWordWithInfo(wordWithInfo: WordWithInfo): Long {
        return _insertWord(wordWithInfo.word).let { insertedId ->
            _insertWordInfo(
                wordWithInfo.wordInfo.copy(wordId = insertedId.toInt())
            )
            insertedId
        }
    }

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWordCategoryMap(wordCategoryMap: WordCategoryMap)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun _insertWord(word: Word): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun _insertWordInfo(wordInfo: WordInfo)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun _insertCategory(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun _insertCategoryInfo(categoryInfo: CategoryInfo)
}