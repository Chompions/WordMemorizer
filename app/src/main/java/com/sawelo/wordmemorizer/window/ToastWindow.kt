package com.sawelo.wordmemorizer.window

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import com.sawelo.wordmemorizer.R
import kotlinx.coroutines.*

class ToastWindow(
    private val context: Context,
    private val text: String
) : BaseWindow(context) {

    private var view: ViewGroup? = null
    private var params: WindowManager.LayoutParams? = null
    private var toastText: TextView? = null

    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)

    init {
        coroutineScope.launch {
            showWindow()
            delay(2000L)
            closeWindow()
        }
    }

    override fun setViews(parent: ViewGroup) {
        toastText = parent.findViewById(R.id.windowToast_text)
        toastText?.text = text
    }

    override fun clearViews() {
        toastText = null
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setParams() {
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM
            windowAnimations = android.R.style.Animation_Dialog
            width = context.resources.displayMetrics.widthPixels - 100
        }
    }

    override fun showWindow() {
        view = layoutInflater.inflate(R.layout.window_toast_floating, null) as ViewGroup
        setParams()
        setViews(view!!)
        windowManager.addView(view, params)
    }

    override fun hideWindow() {}
    override fun revealWindow() {}

    override fun closeWindow() {
        coroutineScope.cancel()
        if (view != null) {
            clearViews()
            windowManager.removeView(view)
            view = null
        }
    }
}