package com.sawelo.wordmemorizer.data.preferences.window

import com.sawelo.wordmemorizer.data.preferences.base.BaseProcess

enum class FloatingBubbleProcess : BaseProcess {
    IsRunning {
        override fun processKey(): String = "PROCESS_FLOATING_BUBBLE_IS_RUNNING"
    },
    IsUnwrapped {
        override fun processKey(): String = "PROCESS_FLOATING_BUBBLE_IS_UNWRAPPED"
    },
    IsVisible {
        override fun processKey(): String = "PROCESS_FLOATING_BUBBLE_IS_VISIBLE"
    },
}