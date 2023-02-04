package com.sawelo.wordmemorizer.util

import android.content.Context
import androidx.datastore.preferences.core.*
import com.sawelo.wordmemorizer.dataStore
import com.sawelo.wordmemorizer.util.sorting_utils.BaseSorting
import com.sawelo.wordmemorizer.util.sorting_utils.SortingAnchor
import com.sawelo.wordmemorizer.util.sorting_utils.SortingOrder
import kotlinx.coroutines.flow.first

object PreferencesUtils {
    const val SORTING_ORDER = "SORTING_ORDER"
    const val SORTING_ANCHOR = "SORTING_ANCHOR"

    suspend fun FloatingBubbleProcess.getProcess(context: Context): Boolean {
        val preferences = context.dataStore.data.first()
        return preferences[booleanPreferencesKey(processKey)] ?: false
    }

    suspend fun FloatingBubbleProcess.setProcess(context: Context, boolean: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[booleanPreferencesKey(processKey)] = boolean
        }
    }

    fun setCurrentSortingToPreferences(settings: MutablePreferences, enum: BaseSorting) {
        return when (enum) {
            is SortingOrder -> settings[stringPreferencesKey(SORTING_ORDER)] = enum.name
            is SortingAnchor -> settings[stringPreferencesKey(SORTING_ANCHOR)] = enum.name
            else -> throw Exception("Invalid enum class parameter")
        }
    }

    inline fun <reified T : Enum<T>> obtainCurrentSortingFromPreferences(preferences: Preferences): BaseSorting {
        return when (T::class.java) {
            SortingOrder::class.java -> {
                val currentName = preferences[stringPreferencesKey(SORTING_ORDER)]
                    ?: SortingOrder.ASCENDING.name
                SortingOrder.valueOf(currentName)
            }
            SortingAnchor::class.java -> {
                val currentName = preferences[stringPreferencesKey(SORTING_ANCHOR)]
                    ?: SortingAnchor.RANDOM.name
                SortingAnchor.valueOf(currentName)
            }
            else -> throw Exception("Invalid enum class parameter")
        }
    }
}

enum class SettingsProcess(val processKey: String) {
    FloatingBubbleSetUp("PROCESS_FLOATING_BUBBLE_SETTING_UP"),
    TranslationDownload("PROCESS_OFFLINE_TRANSLATION_DOWNLOADING"),
    DrawDownload("PROCESS_DRAW_CHARACTER_DOWNLOADING")
}

enum class FloatingBubbleProcess(val processKey: String) {
    IsRunning("PROCESS_FLOATING_BUBBLE_IS_RUNNING"),
    IsUnwrapped("PROCESS_FLOATING_BUBBLE_IS_UNWRAPPED"),
    IsVisible("PROCESS_FLOATING_BUBBLE_IS_VISIBLE"),
}