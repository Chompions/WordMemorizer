package com.sawelo.wordmemorizer.ui.window.instance

import android.content.Context
import com.sawelo.wordmemorizer.data.data_class.entity.Category
import com.sawelo.wordmemorizer.ui.ui_util.FloatingDialogUtil
import com.sawelo.wordmemorizer.ui.window.dialog.FloatingAddWordWindow

class FloatingAddWordWindowInstance(
    context: Context,
    floatingDialogUtil: FloatingDialogUtil,
    selectedCategories: List<Category>? = null,
): FloatingAddWordWindow(context, floatingDialogUtil, selectedCategories) {

    override fun showWindow() {
        if (isWindowActive) {
            super.closeWindow()
        }
        super.showWindow()
    }
}