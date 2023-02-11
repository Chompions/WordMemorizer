package com.sawelo.wordmemorizer.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.appcompat.view.ContextThemeWrapper
import com.sawelo.wordmemorizer.MainApplication
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.util.Constants.RECEIVER_CLOSE_ACTION
import com.sawelo.wordmemorizer.util.Constants.RECEIVER_OPEN_ACTION
import com.sawelo.wordmemorizer.util.Constants.RECEIVER_OPEN_FLOATING_DIALOG_REQUEST_CODE
import com.sawelo.wordmemorizer.util.Constants.selectedCategories
import com.sawelo.wordmemorizer.util.FloatingDialogUtils
import com.sawelo.wordmemorizer.window.dialog.FloatingAddWordWindowInstance
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FloatingAddWordWindowReceiver : BroadcastReceiver() {

    @Inject lateinit var floatingDialogUtils: FloatingDialogUtils
    override fun onReceive(context: Context, intent: Intent) {
        val contextThemeWrapper = ContextThemeWrapper(context, R.style.Theme_WordMemorizer)
        FloatingAddWordWindowInstance(
            contextThemeWrapper, floatingDialogUtils, selectedCategories.value
        ).also { instance ->
            when (intent.action) {
                RECEIVER_CLOSE_ACTION -> instance.closeInstance()
                RECEIVER_OPEN_ACTION -> instance.showInstance()
            }
        }
    }

    companion object {
        fun openWindowPendingIntent(context: Context): PendingIntent {
            val receiverIntent = Intent()
            receiverIntent.action = RECEIVER_OPEN_ACTION
            receiverIntent.`package` = MainApplication.PACKAGE_NAME
            return PendingIntent.getBroadcast(
                context, RECEIVER_OPEN_FLOATING_DIALOG_REQUEST_CODE, receiverIntent, PendingIntent.FLAG_IMMUTABLE
            )
        }

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