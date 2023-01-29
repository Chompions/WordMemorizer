package com.sawelo.wordmemorizer

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.sawelo.wordmemorizer.util.Constants.PACKAGE_NAME
import com.sawelo.wordmemorizer.util.Constants.PREFERENCE_DRAW_CHARACTER_KEY
import com.sawelo.wordmemorizer.util.Constants.PREFERENCE_FLOATING_BUBBLE_KEY
import com.sawelo.wordmemorizer.util.Constants.PREFERENCE_OFFLINE_TRANSLATION_KEY
import dagger.hilt.android.HiltAndroidApp

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@HiltAndroidApp
class MainApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        PACKAGE_NAME = applicationContext.packageName
        PREFERENCE_FLOATING_BUBBLE_KEY = getString(R.string.preference_floating_bubble_key)
        PREFERENCE_OFFLINE_TRANSLATION_KEY = getString(R.string.preference_offline_translation_key)
        PREFERENCE_DRAW_CHARACTER_KEY = getString(R.string.preference_draw_character_key)
    }
}