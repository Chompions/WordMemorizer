package com.sawelo.wordmemorizer.util.enum_class

import android.content.Context

interface BaseSorting {
    fun obtainQueryString(): String
    fun obtainText(context: Context): String
    fun obtainId(): Int {
        throw Exception("Must override obtainId")
    }
}