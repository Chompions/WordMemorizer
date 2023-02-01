package com.sawelo.wordmemorizer.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sawelo.wordmemorizer.data.dao.CategoryDao
import com.sawelo.wordmemorizer.data.dao.WordDao
import com.sawelo.wordmemorizer.data.data_class.Category
import com.sawelo.wordmemorizer.data.data_class.Word
import com.sawelo.wordmemorizer.data.data_class.WordCategoryMap
import com.sawelo.wordmemorizer.data.data_class.WordWithCategories
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Word::class, Category::class, WordCategoryMap::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun categoryDao(): CategoryDao

    companion object {

        private val defaultCategoryList = listOf("All", "Important", "Names")
        private val defaultWordList = listOf(
            WordWithCategories(
                Word(
                    wordText = "お早うございます",
                    furiganaText = "おはようございます",
                    definitionText = "good morning",
                    createdTimeMillis = System.currentTimeMillis(),
                ),
                listOf(Category(
                    2, "Important"
                ))
            )
        )

        private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            if (INSTANCE == null) {
                INSTANCE = buildDatabase(context, CoroutineScope(Dispatchers.IO))
            }
            return INSTANCE as AppDatabase
        }

        private fun buildDatabase(context: Context, coroutineScope: CoroutineScope): AppDatabase {
            val callback = object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    coroutineScope.launch {
                        defaultCategoryList.forEach {
                            val category = Category(categoryName = it)
                            getInstance(context).categoryDao()
                                .insertCategory(category)
                        }
                        defaultWordList.forEach {
                            getInstance(context).wordDao()
                                .insertWordWithCategories(it)
                        }
                    }
                }
            }

            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "word-memorizer-database"
            )
                .addCallback(callback)
                .build()
        }
    }
}