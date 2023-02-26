package com.sawelo.wordmemorizer.data.repository

import android.content.Context
import androidx.core.content.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.preference.PreferenceManager
import com.sawelo.wordmemorizer.data.preferences.SortingHelper
import com.sawelo.wordmemorizer.data.preferences.SortingHelper.setCurrentSorting
import com.sawelo.wordmemorizer.data.preferences.base.BaseProcess
import com.sawelo.wordmemorizer.data.preferences.base.BaseSorting
import com.sawelo.wordmemorizer.data.preferences.settings.SettingsSwitch
import com.sawelo.wordmemorizer.data.preferences.sorting.SortingAnchor
import com.sawelo.wordmemorizer.data.preferences.sorting.SortingOrder
import com.sawelo.wordmemorizer.dataStore
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

class PreferenceRepository(
    val context: Context,
) {

    suspend inline fun <reified T : BaseSorting> getCurrentSortingId(): Int = runBlocking {
        context.dataStore.data.first().let { preferences ->
            SortingHelper.getCurrentSortingId<T>(preferences)
        }
    }

    fun getCurrentSQLSortingStringFlow(): Flow<String> = callbackFlow {
         context.dataStore.data.cancellable().collectLatest { preferences ->
            val sortingAnchor = SortingHelper.getCurrentSortingQuery<SortingAnchor>(preferences)
            val sortingOrder = SortingHelper.getCurrentSortingQuery<SortingOrder>(preferences)
            send("$sortingAnchor $sortingOrder")
        }
        awaitClose { cancel() }
    }

    fun setCurrentSorting(sorting: BaseSorting) {
        runBlocking {
            context.dataStore.edit { preferences ->
                sorting.setCurrentSorting(preferences)
            }
        }
    }

    // Processes changes

    fun getCurrentProcessFlow(process: BaseProcess): Flow<Boolean> = callbackFlow {
        context.dataStore.data.collectLatest {
            send(it[booleanPreferencesKey(process.processKey())] ?: false)
        }
    }

    fun getCurrentProcessSnapshot(process: BaseProcess): Boolean = runBlocking {
        context.dataStore.data.first().let {
            it[booleanPreferencesKey(process.processKey())] ?: false
        }
    }

    fun setCurrentProcess(process: BaseProcess, boolean: Boolean) {
        runBlocking {
            context.dataStore.edit { preferences ->
                preferences[booleanPreferencesKey(process.processKey())] = boolean
            }
        }
    }

    // Switches changes

    fun getCurrentSwitch(switch: SettingsSwitch): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(switch.switchKey, false)
    }

    fun setCurrentSwitch(switch: SettingsSwitch, boolean: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putBoolean(switch.switchKey, boolean)
        }
    }
}