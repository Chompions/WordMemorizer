package com.sawelo.wordmemorizer.window.dialog

import android.content.Context
import com.sawelo.wordmemorizer.data.WordRepository
import com.sawelo.wordmemorizer.data.data_class.Category

class FloatingAddWordWindowInstance(
    private val context: Context,
    private val wordRepository: WordRepository,
    private val currentCategory: Category?,
) {
    private var windowInstance: FloatingAddWordWindow? = null
    fun showInstance() {
        if (FloatingAddWordWindow.isAddWordWindowActive) {
            closeInstance()
        } else {
            windowInstance = FloatingAddWordWindow(context, wordRepository, currentCategory)
            windowInstance?.showWindow()
        }
    }

    fun closeInstance() {
        windowInstance?.closeWindow()
        windowInstance = null
    }
}