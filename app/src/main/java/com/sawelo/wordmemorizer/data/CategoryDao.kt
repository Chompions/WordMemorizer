package com.sawelo.wordmemorizer.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM category ORDER BY id")
    fun getCategories(): Flow<List<Category>>

    @Query ("UPDATE category SET wordCount = wordCount + :changeByInt WHERE id = :id")
    suspend fun updateWordCountById(id: Int, changeByInt: Int = 1)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: Category): Long

    @Delete
    suspend fun deleteCategory(category: Category)
}