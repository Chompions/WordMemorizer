package com.sawelo.wordmemorizer.di

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.sawelo.wordmemorizer.data.AppDatabase
import com.sawelo.wordmemorizer.data.WordRepository
import com.sawelo.wordmemorizer.data.remote.JishoService
import com.sawelo.wordmemorizer.dataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
class WordModule {

    @Provides
    fun provideWordRepository(
        @ApplicationContext context: Context
    ): WordRepository {
        val appDatabase = AppDatabase.getInstance(context)
        val dataStore = context.dataStore
        val jishoService = provideJishoService()
        return WordRepository(appDatabase, dataStore, jishoService)
    }

    @Provides
    fun provideJishoService(): JishoService {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://jisho.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(JishoService::class.java)
    }

    @Provides
    fun provideSharedPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

}