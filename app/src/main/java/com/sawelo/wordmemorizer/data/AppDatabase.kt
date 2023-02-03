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
import com.sawelo.wordmemorizer.util.Constants
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
        const val DATABASE_NAME = "word-memorizer-database"
        private val DEFAULT_CATEGORY_LIST = listOf("All", "Important", "Names")
        private val DEFAULT_WORD_LIST = listOf(
            WordWithCategories(
                Word(
                    wordText = "お早うございます",
                    furiganaText = "おはようございます",
                    definitionText = "good morning",
                    createdTimeMillis = System.currentTimeMillis(),
                ),
                listOf(
                    Category(
                        2, "Important"
                    )
                )
            )
        )
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            if (INSTANCE == null) {
                if (context.getDatabasePath(DATABASE_NAME)?.exists() == false) {
                    Constants.isOtherPackageExistInNewInstall =
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
//            val backupDir = File("${context.filesDir}/databases_backup", "${DATABASE_NAME}_backup.db")
            val callback = object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    coroutineScope.launch {
                        DEFAULT_CATEGORY_LIST.forEach {
                            val category = Category(categoryName = it)
                            getInstance(context).categoryDao().insertCategory(category)
                        }
                        DEFAULT_WORD_LIST.forEach {
                            getInstance(context).wordDao().insertWordWithCategories(it)
                        }
                    }
                }
            }
//            return if (backupDir.exists()) {
//                Room.databaseBuilder(
//                    context,
//                    AppDatabase::class.java,
//                    DATABASE_NAME
//                ).addCallback(callback)
//                    .createFromFile(backupDir)
//                    .build()
//            } else {
//
//            }
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                DATABASE_NAME
            ).addCallback(callback)
                .build()
        }
    }
}