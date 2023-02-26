package com.sawelo.wordmemorizer.data.preferences.sorting

import android.content.Context
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.data.preferences.base.BaseSorting

sealed class SortingOrder: BaseSorting {
    object Ascending: SortingOrder() {
        override fun obtainQueryString(): String = "ASC"
        override fun obtainText(context: Context): String = context.getString(R.string.ascending)
        override fun obtainPrefKey(): String = "ASCENDING"
        override fun obtainId(): Int  = 1
    }
    object Descending: SortingOrder() {
        override fun obtainQueryString(): String = "DESC"
        override fun obtainText(context: Context): String = context.getString(R.string.descending)
        override fun obtainPrefKey(): String = "DESCENDING"
        override fun obtainId(): Int  = 2
    }
}