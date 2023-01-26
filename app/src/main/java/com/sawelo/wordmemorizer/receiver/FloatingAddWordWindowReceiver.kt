package com.sawelo.wordmemorizer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.appcompat.view.ContextThemeWrapper
import com.sawelo.wordmemorizer.MainApplication.Companion.PACKAGE_NAME
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.data.WordRepository
import com.sawelo.wordmemorizer.data.data_class.Category
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
                @Suppress("DEPRECATION")
                val extra = if (Build.VERSION.SDK_INT >= 33) {
                    intent.getParcelableExtra(CURRENT_CATEGORY_EXTRA, Category::class.java)
                } else {
                    intent.getParcelableExtra(CURRENT_CATEGORY_EXTRA)
                }
                showInstance(contextThemeWrapper, extra)
            }
        }
    }

    private fun showInstance(context: Context, currentCategory: Category?) {
        if (!FloatingAddWordWindow.getIsWindowActive()) {
            windowInstance = FloatingAddWordWindow(context, wordRepository, currentCategory)
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
        private const val CURRENT_CATEGORY_EXTRA = "CURRENT_CATEGORY_EXTRA"
        private val OPEN_ACTION = "$PACKAGE_NAME.action.OPEN_FLOATING_DIALOG"
        private val CLOSE_ACTION = "$PACKAGE_NAME.action.CLOSE_FLOATING_DIALOG"

        fun openWindow(context: Context, currentCategory: Category?) {
            val receiverIntent = Intent()
            receiverIntent.action = OPEN_ACTION
            if (currentCategory != null) {
                receiverIntent.putExtra(CURRENT_CATEGORY_EXTRA, currentCategory)
            }
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