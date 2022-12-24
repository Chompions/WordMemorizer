package com.sawelo.wordmemorizer.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Word::class],
    version = 1
)
@TypeConverters(WordInfoConverter::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun wordDao(): WordDao
}