package com.sawelo.wordmemorizer.di

import android.content.Context
import androidx.room.Room
import com.sawelo.wordmemorizer.data.AppDatabase
import com.sawelo.wordmemorizer.data.WordDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object WordModule {
    @Provides
    fun provideWordDao(@ApplicationContext context: Context): WordDao {
        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "word-memorizer-database"
        ).build()
        return db.wordDao()
    }
}