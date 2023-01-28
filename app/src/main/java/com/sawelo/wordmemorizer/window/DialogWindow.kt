package com.sawelo.wordmemorizer.window

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowManager
import com.sawelo.wordmemorizer.util.callback.BackButtonListener
import com.sawelo.wordmemorizer.view.DialogWindowScrollView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

abstract class DialogWindow(
    private val context: Context,
    private val layout: Int
) : BaseWindow(context), OnTouchListener, BackButtonListener {

    private var view: DialogWindowScrollView? = null
    private var params: WindowManager.LayoutParams? = null

    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)

    abstract fun beforeShowWindow(coroutineScope: CoroutineScope)
    abstract fun beforeCloseWindow(coroutineScope: CoroutineScope)

    fun setWidth(widthPixels: Int) {
        params?.width = widthPixels
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setParams() {
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            windowAnimations = android.R.style.Animation_Dialog
            width = context.resources.displayMetrics.widthPixels - 100
        }
        view?.apply {
            setOnTouchListener(this@DialogWindow)
            setBackButtonListener(this@DialogWindow)
        }
    }

    override fun showWindow() {
        view = layoutInflater.inflate(layout, null) as DialogWindowScrollView
        setParams()
        setViews(view!!)
        beforeShowWindow(coroutineScope)
        windowManager.addView(view, params)
    }

    override fun hideWindow() {
        view?.visibility = View.INVISIBLE
    }

    override fun revealWindow() {
        view?.visibility = View.VISIBLE
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
            closeWindow()
        }
        return false
    }

    override fun onBackButtonListener() {
        closeWindow()
    }

    fun showToast(text: String) {
        ToastWindow(context, text)
    }
}