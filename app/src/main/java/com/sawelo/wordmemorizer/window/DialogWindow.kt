package com.sawelo.wordmemorizer.window

import android.content.Context
import android.graphics.PixelFormat
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.WindowManager
import kotlinx.coroutines.*

abstract class DialogWindow(
    context: Context,
    private val layout: Int
): BaseWindow(context), OnTouchListener {
    private var view: ViewGroup? = null
    private val params: WindowManager.LayoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
        PixelFormat.TRANSLUCENT
    ).apply {
        windowAnimations = android.R.style.Animation_Dialog
        width = context.resources.displayMetrics.widthPixels - 100
    }

    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main) + Job()

    abstract fun beforeShowWindow(coroutineScope: CoroutineScope)
    abstract fun beforeCloseWindow(coroutineScope: CoroutineScope)

    fun setWidth(widthPixels: Int) {
        params.width = widthPixels
    }

    override fun showWindow() {
        view = (layoutInflater.inflate(layout, null) as ViewGroup).apply {
            setOnTouchListener(this@DialogWindow)
        }
        setViews(view!!)
        beforeShowWindow(coroutineScope)
        windowManager.addView(view, params)
    }

    override fun closeWindow() {
        beforeCloseWindow(coroutineScope)
        coroutineScope.cancel()
        if (view != null) {
            clearViews()
            windowManager.removeView(view)
            view = null
        }
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_OUTSIDE) {
            windowManager.removeView(view)
        }
        return false
    }
}