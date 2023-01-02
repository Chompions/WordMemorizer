package com.sawelo.wordmemorizer.util

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sawelo.wordmemorizer.R

enum class SortingOrder {
    ASCENDING {
        override fun obtainQueryString(): String = "ASC"
        override fun obtainText(context: Context): String = context.getString(R.string.ascending)
    },
    DESCENDING {
        override fun obtainQueryString(): String = "DESC"
        override fun obtainText(context: Context): String = context.getString(R.string.descending)
    };

    abstract fun obtainQueryString(): String
    abstract fun obtainText(context: Context): String

    companion object {
        val obtainPreferencesKey = stringPreferencesKey("SORTING_ORDER")
    }
}