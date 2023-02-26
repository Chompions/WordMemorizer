package com.sawelo.wordmemorizer.data.preferences

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sawelo.wordmemorizer.data.preferences.base.BaseSorting
import com.sawelo.wordmemorizer.data.preferences.sorting.SortingAnchor
import com.sawelo.wordmemorizer.data.preferences.sorting.SortingOrder

object SortingHelper {

    fun BaseSorting.setCurrentSorting(settings: MutablePreferences) {
        when (this) {
            is SortingAnchor -> settings[stringPreferencesKey(SORTING_ANCHOR)] = this.obtainPrefKey()
            is SortingOrder -> settings[stringPreferencesKey(SORTING_ORDER)] = this.obtainPrefKey()
        }
    }

    inline fun <reified T: BaseSorting> getCurrentSortingQuery(preferences: Preferences): String =
        getCurrentSorting<T>(preferences)?.obtainQueryString()
            ?: throw Exception("Failed to get current sorting query string")

    inline fun <reified T: BaseSorting> getCurrentSortingId(preferences: Preferences): Int =
        getCurrentSorting<T>(preferences)?.obtainId()
            ?: throw Exception("Failed to get current sorting id")

    inline fun <reified T: BaseSorting> getCurrentSorting(preferences: Preferences): T? {
        val defaultSortingAnchor = SortingAnchor.CreatedTime
        val defaultSortingOrder = SortingOrder.Ascending

        // Get currently selected sorting in preferences
        val currentStringPrefKey = when (T::class) {
            SortingAnchor::class -> preferences[stringPreferencesKey(SORTING_ANCHOR)]
                ?: defaultSortingAnchor.obtainPrefKey()
            SortingOrder::class -> preferences[stringPreferencesKey(SORTING_ORDER)]
                ?: defaultSortingOrder.obtainPrefKey()
            else -> throw Exception("Type class not recognized")
        }

        // Find class that corresponds with currently selected sorting
        return T::class.sealedSubclasses
            .first { it.objectInstance?.obtainPrefKey() == currentStringPrefKey }
            .objectInstance
    }

    const val SORTING_ANCHOR = "SORTING_ANCHOR"
    const val SORTING_ORDER = "SORTING_ORDER"
}