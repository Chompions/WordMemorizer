package com.sawelo.wordmemorizer.data.preferences.base

import android.content.Context

interface BaseSorting {
    fun obtainQueryString(): String
    fun obtainText(context: Context): String
    fun obtainPrefKey(): String
    fun obtainId(): Int
}