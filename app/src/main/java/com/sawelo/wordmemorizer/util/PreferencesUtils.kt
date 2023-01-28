package com.sawelo.wordmemorizer.util

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sawelo.wordmemorizer.util.Constants.SORTING_ANCHOR
import com.sawelo.wordmemorizer.util.Constants.SORTING_ORDER
import com.sawelo.wordmemorizer.util.sorting_utils.BaseSorting
import com.sawelo.wordmemorizer.util.sorting_utils.SortingAnchor
import com.sawelo.wordmemorizer.util.sorting_utils.SortingOrder

object PreferencesUtils {

    fun setCurrentSortingToPreferences(settings: MutablePreferences, enum: BaseSorting) {
        return when(enum) {
            is SortingOrder -> settings[stringPreferencesKey(SORTING_ORDER)] = enum.name
            is SortingAnchor -> settings[stringPreferencesKey(SORTING_ANCHOR)] = enum.name
            else -> throw Exception("Invalid enum class parameter")
        }
    }

    inline fun <reified T: Enum<T>> obtainCurrentSortingFromPreferences(preferences: Preferences): BaseSorting {
        return when(T::class.java) {
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