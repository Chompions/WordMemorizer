package com.sawelo.wordmemorizer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.appcompat.view.ContextThemeWrapper
import com.sawelo.wordmemorizer.MainApplication
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.ui.ui_util.FloatingDialogUtil
import com.sawelo.wordmemorizer.ui.window.instance.FloatingAddWordWindowInstance
import com.sawelo.wordmemorizer.util.Constants.RECEIVER_CLOSE_ACTION
import com.sawelo.wordmemorizer.util.Constants.RECEIVER_OPEN_ACTION
import com.sawelo.wordmemorizer.util.Constants.selectedCategories
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FloatingAddWordWindowReceiver : BroadcastReceiver() {

    @Inject lateinit var floatingDialogUtil: FloatingDialogUtil
    override fun onReceive(context: Context, intent: Intent) {
        val contextThemeWrapper = ContextThemeWrapper(context, R.style.Theme_WordMemorizer)
        FloatingAddWordWindowInstance(
            contextThemeWrapper, floatingDialogUtil, selectedCategories.value
        ).also { instance ->
            when (intent.action) {
                RECEIVER_CLOSE_ACTION -> instance.closeWindow()
                RECEIVER_OPEN_ACTION -> instance.showWindow()
            }
        }
    }

    companion object {
        fun openWindow(context: Context) {
            val receiverIntent = Intent()
            receiverIntent.action = RECEIVER_OPEN_ACTION
            receiverIntent.`package` = MainApplication.PACKAGE_NAME
            context.sendBroadcast(receiverIntent)
        }

        fun closeWindow(context: Context) {
            val receiverIntent = Intent()
            receiverIntent.action = RECEIVER_CLOSE_ACTION
            receiverIntent.`package` = MainApplication.PACKAGE_NAME
            context.sendBroadcast(receiverIntent)
        }
    }
}