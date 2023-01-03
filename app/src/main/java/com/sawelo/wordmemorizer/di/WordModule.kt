package com.sawelo.wordmemorizer.di

import android.content.Context
import com.sawelo.wordmemorizer.data.AppDatabase
import com.sawelo.wordmemorizer.data.WordRepository
import com.sawelo.wordmemorizer.dataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object WordModule {

    @Provides
    fun provideWordRepository(
        @ApplicationContext context: Context
    ): WordRepository {
        val appDatabase = AppDatabase.getInstance(context)
        val dataStore = context.dataStore
        return WordRepository(appDatabase, dataStore)
    }

}