package com.sawelo.wordmemorizer.ui.window.instance

import android.content.Context
import com.sawelo.wordmemorizer.ui.window.base.ToastWindow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ToastWindowInstance(
    context: Context,
    inputText: String
): ToastWindow(context, inputText) {

    init {
        windowCoroutineScope.launch {
            showWindow()
            delay(4000L)
            closeWindow()
        }
    }
}