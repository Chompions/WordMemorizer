package com.sawelo.wordmemorizer.service.notification.floating_bubble_action

import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

abstract class BaseFloatingBubbleAction(
    private val lifecycleService: LifecycleService,
) {
    private var afterAction: (() -> Unit)? = null

    fun setAfterAction(afterAction: (() -> Unit)): BaseFloatingBubbleAction {
        this.afterAction = afterAction
        return this
    }

    fun startAction() {
        lifecycleService.lifecycleScope.launch {
            setCurrentProcess()
            setNotification()
            afterAction?.invoke()
        }
    }

    abstract suspend fun setNotification()
    abstract suspend fun setCurrentProcess()
}