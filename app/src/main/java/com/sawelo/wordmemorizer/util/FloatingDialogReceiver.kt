package com.sawelo.wordmemorizer.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.appcompat.view.ContextThemeWrapper
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.data.WordRepository
import com.sawelo.wordmemorizer.util.NotificationUtils.NOTIFICATION_START_ACTION
import com.sawelo.wordmemorizer.util.NotificationUtils.NOTIFICATION_STOP_ACTION
import com.sawelo.wordmemorizer.window.FloatingAddWordWindow
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FloatingDialogReceiver : BroadcastReceiver() {
    @Inject
    lateinit var wordRepository: WordRepository
    private var floatingAddWordWindow: FloatingAddWordWindow? = null

    override fun onReceive(context: Context, intent: Intent) {
        val contextThemeWrapper = ContextThemeWrapper(context, R.style.Theme_WordMemorizer)
        when (intent.action) {
            NOTIFICATION_STOP_ACTION -> {
                floatingAddWordWindow?.closeWindow()
                floatingAddWordWindow = null
            }
            NOTIFICATION_START_ACTION -> {
                if (!FloatingAddWordWindow.getIsWindowActive()) {
                    floatingAddWordWindow =
                        FloatingAddWordWindow(contextThemeWrapper, wordRepository)
                    floatingAddWordWindow?.showWindow()
                } else {
                    floatingAddWordWindow?.closeWindow()
                    floatingAddWordWindow = null
                }
            }
        }
    }
}