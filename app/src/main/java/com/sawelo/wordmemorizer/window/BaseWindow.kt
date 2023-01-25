package com.sawelo.wordmemorizer.window

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager

abstract class BaseWindow(context: Context) {
    val layoutInflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    abstract fun showWindow()
    abstract fun closeWindow()
    abstract fun setViews(parent: ViewGroup)
    abstract fun clearViews()
}