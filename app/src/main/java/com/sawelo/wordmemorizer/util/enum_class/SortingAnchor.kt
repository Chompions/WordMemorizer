package com.sawelo.wordmemorizer.util.enum_class

import android.content.Context
import com.sawelo.wordmemorizer.R

enum class SortingAnchor: BaseSorting {
    REMEMBER_COUNT {
        override fun obtainQueryString(): String = "rememberCount"
        override fun obtainText(context: Context): String = context.getString(R.string.remember_count)
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