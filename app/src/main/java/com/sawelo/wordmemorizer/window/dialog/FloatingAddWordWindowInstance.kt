package com.sawelo.wordmemorizer.window.dialog

import android.content.Context
import com.sawelo.wordmemorizer.data.data_class.entity.Category
import com.sawelo.wordmemorizer.util.FloatingDialogUtils

class FloatingAddWordWindowInstance(
    private val context: Context,
    private val floatingDialogUtils: FloatingDialogUtils,
    private val selectedCategories: List<Category>? = null,
) {
    private var windowInstance: FloatingAddWordWindow? = null

    fun showInstance() {
        if (FloatingAddWordWindow.isAddWordWindowActive) {
            closeInstance()
        } else {
            windowInstance = FloatingAddWordWindow(context, floatingDialogUtils, selectedCategories)
            windowInstance?.showWindow()
        }
    }

    fun closeInstance() {
        windowInstance?.closeWindow()
        windowInstance = null
    }
}