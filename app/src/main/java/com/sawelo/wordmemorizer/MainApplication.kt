package com.sawelo.wordmemorizer

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.HiltAndroidApp

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@HiltAndroidApp
class MainApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        PACKAGE_NAME = applicationContext.packageName
        OTHER_PACKAGE_NAME = if (applicationContext.packageName == "com.sawelo.wordmemorizer")
            "com.sawelo.wordmemorizer.clean" else "com.sawelo.wordmemorizer"
        ACTIVITY_OTHER_CREATE_BACKUP_DB = "$OTHER_PACKAGE_NAME.action.CREATE_BACKUP_DB"
    }

    companion object {
        var PACKAGE_NAME = ""
        var OTHER_PACKAGE_NAME = ""
        var ACTIVITY_OTHER_CREATE_BACKUP_DB = ""
        var isOtherPackageExistInNewInstall = false
    }
}