package com.sawelo.wordmemorizer.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sawelo.wordmemorizer.MainApplication
import com.sawelo.wordmemorizer.data.dao.DeleteDao
import com.sawelo.wordmemorizer.data.dao.InsertDao
import com.sawelo.wordmemorizer.data.dao.QueryDao
import com.sawelo.wordmemorizer.data.dao.UpdateDao
import com.sawelo.wordmemorizer.data.data_class.entity.Category
import com.sawelo.wordmemorizer.data.data_class.entity.CategoryInfo
import com.sawelo.wordmemorizer.data.data_class.entity.Word
import com.sawelo.wordmemorizer.data.data_class.entity.WordInfo
import com.sawelo.wordmemorizer.data.data_class.entity_cross_ref.WordCategoryMap
import com.sawelo.wordmemorizer.data.data_class.relation_ref.CategoryWithInfo
import com.sawelo.wordmemorizer.data.data_class.relation_ref.WordWithCategories
import com.sawelo.wordmemorizer.data.data_class.relation_ref.WordWithInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Word::class,
        WordInfo::class,
        Category::class,
        CategoryInfo::class,
        WordCategoryMap::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun queryDao(): QueryDao
    abstract fun insertDao(): InsertDao
    abstract fun updateDao(): UpdateDao
    abstract fun deleteDao(): DeleteDao

    companion object {
        const val DATABASE_NAME = "word-memorizer-database"
        private val DEFAULT_CATEGORY_LIST = listOf(
            CategoryWithInfo(
                Category(
                    categoryName = "Daily",
                    categoryDesc = "For daily conversation"
                ),
                CategoryInfo()
            ),
            CategoryWithInfo(
                Category(
                    categoryName = "Fun",
                    categoryDesc = "Fun words to remember"
                ),
                CategoryInfo()
            ),
        )

        private fun defaultDailyWord(categoryId: Int) = WordWithCategories(
            WordWithInfo(
                Word(
                    wordText = "お早うございます",
                    furiganaText = "おはようございます",
                    definitionText = "good morning"
                ),
                WordInfo(
                    createdTimeMillis = System.currentTimeMillis()
                )
            ),
            listOf(
                Category(
                    categoryId, "Daily"
                )
            )
        )

        private fun defaultFunWord(categoryId: Int) = WordWithCategories(
            WordWithInfo(
                Word(
                    wordText = "映画",
                    furiganaText = "えいが",
                    definitionText = "movie"
                ),
                WordInfo(
                    createdTimeMillis = System.currentTimeMillis()
                )
            ),
            listOf(
                Category(
                    categoryId, "Fun"
                )
            )
        )

        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            if (INSTANCE == null) {
                if (context.getDatabasePath(DATABASE_NAME)?.exists() == false) {
                    MainApplication.isOtherPackageExistInNewInstall =
                        DatabaseHelper(context).checkIsOtherPackageInstalled()
                }
                INSTANCE = buildBlankDatabase(context)
            }
            return INSTANCE as AppDatabase
        }

        private fun buildBlankDatabase(
            context: Context,
        ): AppDatabase {
            val coroutineScope = CoroutineScope(Dispatchers.IO)
            val callback = object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    coroutineScope.launch {
                        DEFAULT_CATEGORY_LIST.forEach {
                            getInstance(context).insertDao().insertCategoryWithInfo(it)
                                .let { categoryId ->
                                    if (it.category.categoryName == "Daily") {
                                        getInstance(context).insertDao().insertWordWithCategories(
                                            defaultDailyWord(categoryId.toInt())
                                        )
                                    }
                                    if (it.category.categoryName == "Fun") {
                                        getInstance(context).insertDao().insertWordWithCategories(
                                            defaultFunWord(categoryId.toInt())
                                        )
                                    }
                                    getInstance(context).updateDao().updateWordCountCategory(
                                        categoryId.toInt(), 1
                                    )
                                }
                        }
                    }
                }
            }
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                DATABASE_NAME
            ).addCallback(callback)
                .build()
        }
    }
}