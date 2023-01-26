package com.sawelo.wordmemorizer.util.sorting_utils

import android.content.Context
import com.sawelo.wordmemorizer.R

enum class SortingAnchor: BaseSorting {
    FORGOT_COUNT {
        override fun obtainQueryString(): String = "forgotCount"
        override fun obtainText(context: Context): String = context.getString(R.string.forgot_count)
    },
    CREATED_TIME {
        override fun obtainQueryString(): String = "createdTimeMillis"
        override fun obtainText(context: Context): String = context.getString(R.string.created_time)
    },
    RANDOM {
        override fun obtainQueryString(): String = "RANDOM()"
        override fun obtainText(context: Context): String = context.getString(R.string.random)
    };

    override fun obtainId(): Int = ordinal
}