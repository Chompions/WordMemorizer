package com.sawelo.wordmemorizer.window

import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

abstract class BaseWindow(private val context: Context) {

    private val layoutInflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var viewGroup: ViewGroup? = null

    val windowCoroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
    val windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private fun initialParams(): WindowManager.LayoutParams {
        val windowType = if (context !is Activity && Settings.canDrawOverlays(context)) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else WindowManager.LayoutParams.TYPE_APPLICATION

        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            windowType,
            0,
            PixelFormat.TRANSLUCENT
        ).apply {
            windowAnimations = android.R.style.Animation_Dialog
            width = context.resources.displayMetrics.widthPixels - 100
        }
    }
    fun showWindow() {
        viewGroup = setViews(layoutInflater)
        val params = setParams(initialParams())
        beforeShowWindow()
        windowManager.addView(viewGroup, params)
    }

    fun hideWindow() {
        viewGroup?.visibility = View.INVISIBLE
    }

    fun revealWindow() {
        viewGroup?.visibility = View.VISIBLE
    }

    fun closeWindow() {
        windowCoroutineScope.cancel()
        beforeCloseWindow()
        windowManager.removeView(viewGroup)
        viewGroup = null
    }

    abstract fun setViews(layoutInflater: LayoutInflater): ViewGroup
    abstract fun setParams(params: WindowManager.LayoutParams): WindowManager.LayoutParams
    abstract fun beforeShowWindow()
    abstract fun beforeCloseWindow()

}