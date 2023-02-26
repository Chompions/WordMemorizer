package com.sawelo.wordmemorizer.data.di

import android.content.Context
import com.sawelo.wordmemorizer.data.database.AppDatabase
import com.sawelo.wordmemorizer.data.remote.JishoService
import com.sawelo.wordmemorizer.data.repository.LocalRepository
import com.sawelo.wordmemorizer.data.repository.PreferenceRepository
import com.sawelo.wordmemorizer.data.repository.RemoteRepository
import com.sawelo.wordmemorizer.ui.ui_util.FloatingDialogUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
class ObjectModule {

    @Provides
    fun provideFloatingDialogUtils(
        localRepository: LocalRepository,
        remoteRepository: RemoteRepository
    ): FloatingDialogUtil {
        return FloatingDialogUtil(localRepository, remoteRepository)
    }

    @Provides
    fun provideLocalRepository(
        @ApplicationContext context: Context,
        preferenceRepository: PreferenceRepository
    ): LocalRepository {
        val appDatabase = AppDatabase.getInstance(context)
        return LocalRepository(appDatabase, preferenceRepository)
    }

    @Provides
    fun provideRemoteRepository(): RemoteRepository {
        val jishoService = provideJishoService()
        return RemoteRepository(jishoService)
    }

    @Provides
    fun providePreferenceRepository(
        @ApplicationContext context: Context
    ): PreferenceRepository {
        return PreferenceRepository(context)
    }

    @Provides
    fun provideJishoService(): JishoService {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://jisho.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(JishoService::class.java)
    }
//
//    @Provides
//    fun provideSharedPreferences(
//        @ApplicationContext context: Context
//    ): SharedPreferences {
//        return PreferenceManager.getDefaultSharedPreferences(context)
//    }
}