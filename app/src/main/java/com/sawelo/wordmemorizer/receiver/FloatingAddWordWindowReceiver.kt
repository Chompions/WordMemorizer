package com.sawelo.wordmemorizer.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.view.ContextThemeWrapper
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.data.WordRepository
import com.sawelo.wordmemorizer.data.data_class.Category
import com.sawelo.wordmemorizer.util.Constants
import com.sawelo.wordmemorizer.util.Constants.RECEIVER_OPEN_FLOATING_DIALOG_REQUEST_CODE
import com.sawelo.wordmemorizer.util.Constants.RECEIVER_CLOSE_ACTION
import com.sawelo.wordmemorizer.util.Constants.RECEIVER_OPEN_ACTION
import com.sawelo.wordmemorizer.window.dialog.FloatingAddWordWindowInstance
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FloatingAddWordWindowReceiver : BroadcastReceiver() {
    @Inject
    lateinit var wordRepository: WordRepository

    override fun onReceive(context: Context, intent: Intent) {
        println("you receive something")

        @Suppress("DEPRECATION")
        val extra = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra(CURRENT_CATEGORY_EXTRA, Category::class.java)
        } else {
            intent.getParcelableExtra(CURRENT_CATEGORY_EXTRA)
        }
        val contextThemeWrapper = ContextThemeWrapper(context, R.style.Theme_WordMemorizer)

        FloatingAddWordWindowInstance(
            contextThemeWrapper, wordRepository, extra
        ).also { instance ->
            when (intent.action) {
                RECEIVER_CLOSE_ACTION -> instance.closeInstance()
                RECEIVER_OPEN_ACTION -> instance.showInstance()
            }
        }
    }

    companion object {
        private const val CURRENT_CATEGORY_EXTRA = "CURRENT_CATEGORY_EXTRA"

        fun openWindowPendingIntent(context: Context, currentCategory: Category?): PendingIntent {
            val receiverIntent = Intent()
            receiverIntent.action = RECEIVER_OPEN_ACTION
            receiverIntent.`package` = Constants.PACKAGE_NAME
            if (currentCategory != null) {
                receiverIntent.putExtra(CURRENT_CATEGORY_EXTRA, currentCategory)
            }
            return PendingIntent.getBroadcast(
                context, RECEIVER_OPEN_FLOATING_DIALOG_REQUEST_CODE, receiverIntent, PendingIntent.FLAG_IMMUTABLE
            )
        }

        fun openWindow(context: Context, currentCategory: Category?) {
            val receiverIntent = Intent()
            receiverIntent.action = RECEIVER_OPEN_ACTION
            receiverIntent.`package` = Constants.PACKAGE_NAME
            if (currentCategory != null) {
                receiverIntent.putExtra(CURRENT_CATEGORY_EXTRA, currentCategory)
            }
            context.sendBroadcast(receiverIntent)
        }

        fun closeWindow(context: Context) {
            val receiverIntent = Intent()
            receiverIntent.action = RECEIVER_CLOSE_ACTION
            receiverIntent.`package` = Constants.PACKAGE_NAME
            context.sendBroadcast(receiverIntent)
        }
    }
}