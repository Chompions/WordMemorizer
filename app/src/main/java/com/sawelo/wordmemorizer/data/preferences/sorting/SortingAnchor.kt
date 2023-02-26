package com.sawelo.wordmemorizer.data.preferences.sorting

import android.content.Context
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.data.preferences.base.BaseSorting

sealed class SortingAnchor: BaseSorting {
    object CreatedTime: SortingAnchor() {
        override fun obtainQueryString(): String = "createdTimeMillis"
        override fun obtainText(context: Context): String = context.getString(R.string.created_time)
        override fun obtainPrefKey(): String = "CREATED_TIME"
        override fun obtainId(): Int  = 1
    }
    object RememberCount : SortingAnchor() {
        override fun obtainQueryString(): String = "rememberCount"
        override fun obtainText(context: Context): String = context.getString(R.string.remember_count)
        override fun obtainPrefKey(): String = "REMEMBER_COUNT"
        override fun obtainId(): Int  = 2
    }
    object Random: SortingAnchor() {
        override fun obtainQueryString(): String = "RANDOM()"
        override fun obtainText(context: Context): String = context.getString(R.string.random)
        override fun obtainPrefKey(): String = "RANDOM"
        override fun obtainId(): Int  = 3
    }
}