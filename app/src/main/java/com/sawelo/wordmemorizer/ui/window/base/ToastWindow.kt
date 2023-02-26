package com.sawelo.wordmemorizer.ui.window.base

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import com.sawelo.wordmemorizer.databinding.WindowToastFloatingBinding

abstract class ToastWindow(
    context: Context,
    private val inputText: String
) : BaseWindow(context) {
    private var binding: WindowToastFloatingBinding? = null

    override fun setViews(layoutInflater: LayoutInflater): ViewGroup {
        binding = WindowToastFloatingBinding.inflate(layoutInflater)
        return binding?.root ?: throw Exception ("Binding cannot be null")
    }

    override fun setParams(params: WindowManager.LayoutParams): WindowManager.LayoutParams {
        return params.apply {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            gravity = Gravity.BOTTOM
        }
    }

    override fun beforeShowWindow() {
        binding?.windowToastText?.text = inputText
    }

    override fun beforeCloseWindow() {}
}