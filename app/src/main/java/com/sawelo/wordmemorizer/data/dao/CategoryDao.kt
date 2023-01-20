package com.sawelo.wordmemorizer.data.dao

import androidx.room.*
import com.sawelo.wordmemorizer.data.data_class.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM category ORDER BY categoryId")
    fun getCategoriesName(): Flow<List<Category>>

    @Query ("UPDATE category SET wordCount = wordCount + 1 WHERE categoryId = :categoryId")
    suspend fun incrementWordCountById(categoryId: Int)

    @Query ("UPDATE category SET wordCount = wordCount - 1 WHERE categoryId = :categoryId")
    suspend fun decrementWordCountById(categoryId: Int)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: Category): Long

    @Delete
    suspend fun deleteCategory(category: Category)
}