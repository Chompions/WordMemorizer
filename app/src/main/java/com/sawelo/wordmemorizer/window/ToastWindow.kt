package com.sawelo.wordmemorizer.window

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import com.sawelo.wordmemorizer.databinding.WindowToastFloatingBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ToastWindow(
    context: Context,
    private val inputText: String
) : BaseWindow(context) {
    private var binding: WindowToastFloatingBinding? = null

    init {
        windowCoroutineScope.launch {
            showWindow()
            delay(4000L)
            closeWindow()
        }
    }
    override fun setViews(layoutInflater: LayoutInflater): ViewGroup {
        binding = WindowToastFloatingBinding.inflate(layoutInflater)
        return binding?.root ?: throw Exception ("Binding cannot be null")
    }

    override fun setParams(params: WindowManager.LayoutParams): WindowManager.LayoutParams {
        return params.apply {
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