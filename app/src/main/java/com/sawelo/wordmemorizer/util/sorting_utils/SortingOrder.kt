package com.sawelo.wordmemorizer.util.sorting_utils

import android.content.Context
import com.sawelo.wordmemorizer.R

enum class SortingOrder: Sorting {
    ASCENDING {
        override fun obtainQueryString(): String = "ASC"
        override fun obtainText(context: Context): String = context.getString(R.string.ascending)
    },
    DESCENDING {
        override fun obtainQueryString(): String = "DESC"
        override fun obtainText(context: Context): String = context.getString(R.string.descending)
    };

    override fun obtainId(): Int = ordinal
}