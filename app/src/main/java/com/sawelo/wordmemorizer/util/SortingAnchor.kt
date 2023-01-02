package com.sawelo.wordmemorizer.util

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sawelo.wordmemorizer.R

enum class SortingAnchor {
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

    abstract fun obtainQueryString(): String
    abstract fun obtainText(context: Context): String

    companion object {
        val obtainPreferencesKey = stringPreferencesKey("SORTING_ANCHOR")
    }
}