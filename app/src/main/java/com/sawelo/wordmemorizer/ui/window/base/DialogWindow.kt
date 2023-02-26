package com.sawelo.wordmemorizer.ui.window.base

import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowManager
import com.sawelo.wordmemorizer.util.callback.BackButtonListener

abstract class DialogWindow(
    context: Context
): BaseWindow(context), OnTouchListener, BackButtonListener {

    override fun setParams(params: WindowManager.LayoutParams): WindowManager.LayoutParams {
        return params.apply {
            flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        }
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_OUTSIDE) {
            view.performClick()
            closeWindow()
        }
        return false
    }

    override fun onBackButtonListener() {
        closeWindow()
    }
}