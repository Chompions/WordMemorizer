package com.sawelo.wordmemorizer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.view.ContextThemeWrapper
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.data.WordRepository
import com.sawelo.wordmemorizer.window.FloatingAddWordWindow
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FloatingAddWordWindowReceiver : BroadcastReceiver() {
    @Inject
    lateinit var wordRepository: WordRepository
    private var windowInstance: FloatingAddWordWindow? = null

    override fun onReceive(context: Context, intent: Intent) {
        val contextThemeWrapper = ContextThemeWrapper(context, R.style.Theme_WordMemorizer)

        when (intent.action) {
            CLOSE_ACTION -> closeInstance()
            OPEN_ACTION -> {
                showInstance(contextThemeWrapper)
            }
        }
    }

    private fun showInstance(context: Context) {
        if (!FloatingAddWordWindow.getIsWindowActive()) {
            windowInstance = FloatingAddWordWindow(context, wordRepository)
            windowInstance?.showWindow()
        } else {
            windowInstance?.closeWindow()
            windowInstance = null
        }
    }

    private fun closeInstance() {
        windowInstance?.closeWindow()
        windowInstance = null
    }

    companion object {
        const val OPEN_ACTION = "com.sawelo.wordmemorizer.action.OPEN_FLOATING_DIALOG"
        const val CLOSE_ACTION = "com.sawelo.wordmemorizer.action.CLOSE_FLOATING_DIALOG"

        fun openWindow(context: Context) {
            val receiverIntent = Intent()
            receiverIntent.action = OPEN_ACTION
            context.sendBroadcast(receiverIntent)
        }

        fun closeWindow(context: Context) {
            val receiverIntent = Intent()
            receiverIntent.action = CLOSE_ACTION
            context.sendBroadcast(receiverIntent)
        }

        fun FloatingAddWordWindowReceiver.registerReceiver(context: Context) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(OPEN_ACTION)
            intentFilter.addAction(CLOSE_ACTION)
            context.registerReceiver(this, intentFilter)
        }

        fun FloatingAddWordWindowReceiver.unregisterReceiver(context: Context) {
            context.unregisterReceiver(this)
        }
    }
}