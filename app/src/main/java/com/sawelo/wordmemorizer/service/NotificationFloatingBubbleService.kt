package com.sawelo.wordmemorizer.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.receiver.FloatingAddWordWindowReceiver
import com.sawelo.wordmemorizer.util.Constants
import com.sawelo.wordmemorizer.util.Constants.NOTIFICATION_HIDE_ACTION
import com.sawelo.wordmemorizer.util.Constants.NOTIFICATION_REVEAL_ACTION
import com.sawelo.wordmemorizer.util.Constants.NOTIFICATION_START_ACTION
import com.sawelo.wordmemorizer.util.Constants.NOTIFICATION_STOP_ACTION
import com.sawelo.wordmemorizer.util.Constants.NOTIFICATION_UNWRAP_ACTION
import com.sawelo.wordmemorizer.util.Constants.NOTIFICATION_WRAP_ACTION
import com.sawelo.wordmemorizer.util.Constants.SERVICE_CLOSE_FLOATING_BUBBLE_REQUEST_CODE
import com.sawelo.wordmemorizer.util.Constants.SERVICE_WRAP_FLOATING_BUBBLE_REQUEST_CODE
import com.sawelo.wordmemorizer.util.PreferencesUtils.getProcess
import com.sawelo.wordmemorizer.util.PreferencesUtils.setProcess
import com.sawelo.wordmemorizer.util.enum_class.FloatingBubbleProcess
import com.sawelo.wordmemorizer.util.enum_class.SettingsSwitch
import com.sawelo.wordmemorizer.window.FloatingBubbleWindow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class NotificationFloatingBubbleService : Service() {

    private var floatingBubbleWindow: FloatingBubbleWindow? = null
    private var notificationManager: NotificationManager? = null
    private var coroutineScope: CoroutineScope? = null

    private val service = this

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        coroutineScope = CoroutineScope(Dispatchers.Main)

        when (intent?.action) {
            NOTIFICATION_STOP_ACTION -> if (floatingBubbleWindow != null) stopSelf()
            NOTIFICATION_START_ACTION -> {
                if (floatingBubbleWindow == null && !FloatingBubbleWindow.isWindowActive) {
                    runBlocking {
                        startForeground(NOTIFICATION_ID, createNotificationBuilder().build())
                    }
                    coroutineScope?.launch {
                        FloatingBubbleProcess.IsRunning.setProcess(service, true)
                        FloatingBubbleProcess.IsUnwrapped.setProcess(service, true)
                        FloatingBubbleProcess.IsVisible.setProcess(service, true)
                    }
                    floatingBubbleWindow = FloatingBubbleWindow(service)
                    floatingBubbleWindow?.showWindow()
                }
            }
            NOTIFICATION_UNWRAP_ACTION -> {
                if (intent.getBooleanExtra(IS_SENT_FROM_NOTIF, false)) {
                    Constants.floatingBubbleIsWrappedOnNotif = false
                }
                if (floatingBubbleWindow != null) {
                    coroutineScope?.launch {
                        FloatingBubbleProcess.IsUnwrapped.setProcess(service, true)
                        FloatingBubbleProcess.IsVisible.setProcess(service, true)
                        notificationManager?.notify(
                            NOTIFICATION_ID,
                            createNotificationBuilder().build()
                        )
                    }
                    floatingBubbleWindow?.revealWindow()
                }
            }
            NOTIFICATION_WRAP_ACTION -> {

                if (intent.getBooleanExtra(IS_SENT_FROM_NOTIF, false)) {
                    Constants.floatingBubbleIsWrappedOnNotif = true
                }
                if (floatingBubbleWindow != null) {
                    coroutineScope?.launch {
                        FloatingBubbleProcess.IsUnwrapped.setProcess(service, false)
                        FloatingBubbleProcess.IsVisible.setProcess(service, false)
                        notificationManager?.notify(
                            NOTIFICATION_ID,
                            createNotificationBuilder().build()
                        )
                    }
                    floatingBubbleWindow?.hideWindow()
                }
            }
            NOTIFICATION_REVEAL_ACTION -> {
                if (floatingBubbleWindow != null) {
                    coroutineScope?.launch {
                        FloatingBubbleProcess.IsVisible.setProcess(service, true)
                        notificationManager?.notify(
                            NOTIFICATION_ID,
                            createNotificationBuilder().build()
                        )
                        if (FloatingBubbleProcess.IsUnwrapped.getProcess(service)) {
                            floatingBubbleWindow?.revealWindow()
                        }
                    }
                }
            }
            NOTIFICATION_HIDE_ACTION -> {
                if (floatingBubbleWindow != null) {
                    coroutineScope?.launch {
                        FloatingBubbleProcess.IsVisible.setProcess(service, false)
                        notificationManager?.notify(
                            NOTIFICATION_ID,
                            createNotificationBuilder().build()
                        )
                        if (FloatingBubbleProcess.IsUnwrapped.getProcess(service)) {
                            floatingBubbleWindow?.hideWindow()
                        }
                    }
                }
            }
            else -> throw Exception("Intent action is not recognizable")
        }
        return START_STICKY
    }

    override fun onDestroy() {
        coroutineScope?.launch {
            PreferenceManager.getDefaultSharedPreferences(service).edit {
                putBoolean(SettingsSwitch.FloatingBubbleSwitch.switchKey, false)
            }
            FloatingBubbleProcess.IsRunning.setProcess(service, false)
            FloatingBubbleProcess.IsUnwrapped.setProcess(service, false)
            FloatingBubbleProcess.IsVisible.setProcess(service, false)
        }

        FloatingAddWordWindowReceiver.closeWindow(this)
        floatingBubbleWindow?.closeWindow()
        floatingBubbleWindow = null
        notificationManager = null
        coroutineScope = null
        super.onDestroy()
    }

    private suspend fun createNotificationBuilder(): NotificationCompat.Builder {
        // Create notification channel
        val name = "Floating Bubble Channel"
        val descriptionText = "Channel for adjusting floating bubble"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        notificationManager?.createNotificationChannel(channel)

        val wrapServiceIntent = Intent(this, NotificationFloatingBubbleService::class.java)
        wrapServiceIntent.action = if (FloatingBubbleProcess.IsUnwrapped.getProcess(this))
            NOTIFICATION_WRAP_ACTION else NOTIFICATION_UNWRAP_ACTION
        wrapServiceIntent.putExtra(IS_SENT_FROM_NOTIF, true)
        val wrapServicePendingIntent = PendingIntent.getService(
            this,
            SERVICE_WRAP_FLOATING_BUBBLE_REQUEST_CODE,
            wrapServiceIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopServiceIntent = Intent(this, NotificationFloatingBubbleService::class.java)
        stopServiceIntent.action = NOTIFICATION_STOP_ACTION
        val stopServicePendingIntent = PendingIntent.getService(
            this,
            SERVICE_CLOSE_FLOATING_BUBBLE_REQUEST_CODE,
            stopServiceIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val largeIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_stat_name)
        val openDialogPendingIntent =
            FloatingAddWordWindowReceiver.openWindowPendingIntent(this)

        // Create notification builder
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setLargeIcon(largeIcon)
            .setContentTitle("Tap to add word")
            .setContentText("Tap here to open the add dialog")
            .setShowWhen(false)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setContentIntent(openDialogPendingIntent)
            .addAction(
                R.drawable.baseline_visibility_24,
                if (FloatingBubbleProcess.IsUnwrapped.getProcess(this))
                    "Hide floating bubble" else "Show floating bubble",
                wrapServicePendingIntent
            )
            .addAction(
                R.drawable.ic_baseline_close_24,
                "Turn off",
                stopServicePendingIntent
            )
    }

    companion object {
        private const val CHANNEL_ID = "FLOATING_BUBBLE_CHANNEL_ID"
        private const val NOTIFICATION_ID = 1
        private const val IS_SENT_FROM_NOTIF = "IS_SENT_FROM_NOTIF"

        fun startService(context: Context) {
            val serviceIntent = Intent(context, NotificationFloatingBubbleService::class.java)
            serviceIntent.action = NOTIFICATION_START_ACTION
            context.startForegroundService(serviceIntent)
        }

        fun wrapBubbleService(context: Context) {
            val serviceIntent = Intent(context, NotificationFloatingBubbleService::class.java)
            serviceIntent.action = NOTIFICATION_WRAP_ACTION
            context.startService(serviceIntent)
        }

        fun unwrapBubbleService(context: Context) {
            val serviceIntent = Intent(context, NotificationFloatingBubbleService::class.java)
            serviceIntent.action = NOTIFICATION_UNWRAP_ACTION
            context.startService(serviceIntent)
        }

        fun hideBubbleService(context: Context) {
            val serviceIntent = Intent(context, NotificationFloatingBubbleService::class.java)
            serviceIntent.action = NOTIFICATION_HIDE_ACTION
            context.startService(serviceIntent)
        }

        fun revealBubbleService(context: Context) {
            val serviceIntent = Intent(context, NotificationFloatingBubbleService::class.java)
            serviceIntent.action = NOTIFICATION_REVEAL_ACTION
            context.startService(serviceIntent)
        }

        fun stopService(context: Context) {
            val serviceIntent = Intent(context, NotificationFloatingBubbleService::class.java)
            serviceIntent.action = NOTIFICATION_STOP_ACTION
            context.stopService(serviceIntent)
        }
    }
}