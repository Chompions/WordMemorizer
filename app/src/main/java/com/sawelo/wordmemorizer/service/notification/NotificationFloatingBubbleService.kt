package com.sawelo.wordmemorizer.service.notification

import android.content.Context
import android.content.Intent
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.sawelo.wordmemorizer.data.preferences.window.FloatingBubbleProcess
import com.sawelo.wordmemorizer.data.repository.PreferenceRepository
import com.sawelo.wordmemorizer.dataStore
import com.sawelo.wordmemorizer.service.notification.FloatingBubbleNotification.Companion.IS_SENT_FROM_NOTIF
import com.sawelo.wordmemorizer.service.notification.floating_bubble_action.*
import com.sawelo.wordmemorizer.ui.window.base.FloatingBubbleWindow
import com.sawelo.wordmemorizer.util.Constants
import com.sawelo.wordmemorizer.util.Constants.NOTIFICATION_HIDE_ACTION
import com.sawelo.wordmemorizer.util.Constants.NOTIFICATION_REVEAL_ACTION
import com.sawelo.wordmemorizer.util.Constants.NOTIFICATION_START_ACTION
import com.sawelo.wordmemorizer.util.Constants.NOTIFICATION_STOP_ACTION
import com.sawelo.wordmemorizer.util.Constants.NOTIFICATION_UNWRAP_ACTION
import com.sawelo.wordmemorizer.util.Constants.NOTIFICATION_WRAP_ACTION
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationFloatingBubbleService : LifecycleService() {

    @Inject
    lateinit var preferenceRepository: PreferenceRepository
    private var windowInstance: FloatingBubbleWindow? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (windowInstance == null) windowInstance = FloatingBubbleWindow(this)

        lifecycleScope.launch {
            dataStore.data.distinctUntilChanged().collectLatest {
                if (it[booleanPreferencesKey(FloatingBubbleProcess.IsUnwrapped.processKey())] == true &&
                    it[booleanPreferencesKey(FloatingBubbleProcess.IsVisible.processKey())] == true
                ) windowInstance?.revealWindow()
                else windowInstance?.hideWindow()
            }
        }

        when (intent?.action) {
            NOTIFICATION_START_ACTION -> {
                FloatingBubbleStartAction(this, preferenceRepository)
                    .setAfterAction {
                        windowInstance?.showWindow()
                    }
                    .startAction()
            }

            NOTIFICATION_UNWRAP_ACTION -> {
                if (intent.getBooleanExtra(IS_SENT_FROM_NOTIF, false))
                    Constants.floatingBubbleIsWrappedOnNotif = false
                FloatingBubbleUnwrapAction(this, preferenceRepository).startAction()
            }

            NOTIFICATION_REVEAL_ACTION ->
                FloatingBubbleRevealAction(this, preferenceRepository).startAction()

            NOTIFICATION_HIDE_ACTION ->
                FloatingBubbleHideAction(this, preferenceRepository).startAction()

            NOTIFICATION_WRAP_ACTION -> {
                if (intent.getBooleanExtra(IS_SENT_FROM_NOTIF, false))
                    Constants.floatingBubbleIsWrappedOnNotif = true
                FloatingBubbleWrapAction(this, preferenceRepository).startAction()
            }

            NOTIFICATION_STOP_ACTION ->
                stopSelf()

            else -> throw Exception("Intent action is not recognizable")
        }

        return START_STICKY
    }

    override fun onDestroy() {
        FloatingBubbleStopAction(this, preferenceRepository)
            .setAfterAction {
                windowInstance?.closeWindow()
                super.onDestroy()
            }
            .startAction()
    }

    companion object {
        fun startService(context: Context) {
            val serviceIntent = Intent(context, NotificationFloatingBubbleService::class.java)
            serviceIntent.action = NOTIFICATION_START_ACTION
            context.startForegroundService(serviceIntent)
        }

        fun unwrapBubbleService(context: Context) {
            val serviceIntent = Intent(context, NotificationFloatingBubbleService::class.java)
            serviceIntent.action = NOTIFICATION_UNWRAP_ACTION
            context.startService(serviceIntent)
        }

        fun revealBubbleService(context: Context) {
            val serviceIntent = Intent(context, NotificationFloatingBubbleService::class.java)
            serviceIntent.action = NOTIFICATION_REVEAL_ACTION
            context.startService(serviceIntent)
        }

        fun hideBubbleService(context: Context) {
            val serviceIntent = Intent(context, NotificationFloatingBubbleService::class.java)
            serviceIntent.action = NOTIFICATION_HIDE_ACTION
            context.startService(serviceIntent)
        }

        fun wrapBubbleService(context: Context) {
            val serviceIntent = Intent(context, NotificationFloatingBubbleService::class.java)
            serviceIntent.action = NOTIFICATION_WRAP_ACTION
            context.startService(serviceIntent)
        }

        fun stopService(context: Context) {
            val serviceIntent = Intent(context, NotificationFloatingBubbleService::class.java)
            serviceIntent.action = NOTIFICATION_STOP_ACTION
            context.stopService(serviceIntent)
        }
    }
}