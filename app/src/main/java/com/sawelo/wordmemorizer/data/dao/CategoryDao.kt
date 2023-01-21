package com.sawelo.wordmemorizer.data.dao

import androidx.room.*
import com.sawelo.wordmemorizer.data.data_class.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM category ORDER BY categoryId")
    fun getCategoryList(): Flow<List<Category>>

    @Query ("UPDATE category SET wordCount = :wordCount WHERE categoryId = :categoryId")
    suspend fun setWordCountById(categoryId: Int, wordCount: Int)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: Category): Long

    @Delete
    suspend fun deleteCategory(category: Category)
}