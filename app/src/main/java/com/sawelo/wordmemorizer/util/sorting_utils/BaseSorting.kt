package com.sawelo.wordmemorizer.util.sorting_utils

import android.content.Context

interface BaseSorting {
    fun obtainQueryString(): String
    fun obtainText(context: Context): String
    fun obtainId(): Int {
        throw Exception("Must override obtainId")
    }
}