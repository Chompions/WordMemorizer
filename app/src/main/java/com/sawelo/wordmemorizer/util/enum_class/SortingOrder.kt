package com.sawelo.wordmemorizer.util.enum_class

import android.content.Context
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.util.sorting_utils.BaseSorting

enum class SortingOrder: BaseSorting {
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