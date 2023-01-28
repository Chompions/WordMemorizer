package com.sawelo.wordmemorizer.di

import android.content.Context
import com.sawelo.wordmemorizer.data.AppDatabase
import com.sawelo.wordmemorizer.data.WordRepository
import com.sawelo.wordmemorizer.data.remote.JishoService
import com.sawelo.wordmemorizer.data.remote.LingvanexService
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
object WordModule {
    @Provides
    fun provideWordRepository(
        @ApplicationContext context: Context
    ): WordRepository {
        val appDatabase = AppDatabase.getInstance(context)
        val dataStore = context.dataStore
        val jishoService = provideJishoService()
        val lingvanexService = provideLingvanexService()
        return WordRepository(appDatabase, dataStore, jishoService, lingvanexService)
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
    fun provideLingvanexService(): LingvanexService {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://lingvanex-translate.p.rapidapi.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(LingvanexService::class.java)
    }

}