package com.sawelo.wordmemorizer.util

import android.content.Context

interface Sorting {
    fun obtainQueryString(): String
    fun obtainText(context: Context): String
    fun obtainId(): Int {
        throw Exception("Must override obtainId")
    }
}