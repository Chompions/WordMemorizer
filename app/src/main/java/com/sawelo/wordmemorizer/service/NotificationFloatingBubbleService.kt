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
import com.sawelo.wordmemorizer.util.Constants.SERVICE_CLOSE_FLOATING_BUBBLE_REQUEST_CODE
import com.sawelo.wordmemorizer.util.Constants.SERVICE_HIDE_SHOW_FLOATING_BUBBLE_REQUEST_CODE
import com.sawelo.wordmemorizer.util.Constants.NOTIFICATION_HIDE_ACTION
import com.sawelo.wordmemorizer.util.Constants.NOTIFICATION_REVEAL_ACTION
import com.sawelo.wordmemorizer.util.Constants.NOTIFICATION_START_ACTION
import com.sawelo.wordmemorizer.util.Constants.NOTIFICATION_STOP_ACTION
import com.sawelo.wordmemorizer.util.Constants.PREFERENCE_FLOATING_BUBBLE_KEY
import com.sawelo.wordmemorizer.window.BaseWindow
import com.sawelo.wordmemorizer.window.FloatingBubbleWindow

class NotificationFloatingBubbleService : Service() {

    private var floatingBubbleWindow: BaseWindow? = null
    private var notificationManager: NotificationManager? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        when (intent.action) {
            NOTIFICATION_STOP_ACTION -> {
                isFloatingBubbleVisible = false
                stopSelf()
            }
            NOTIFICATION_START_ACTION -> {
                isFloatingBubbleVisible = true
                floatingBubbleWindow = FloatingBubbleWindow(this)
                startForeground(NOTIFICATION_ID, createNotificationBuilder().build())
                floatingBubbleWindow?.showWindow()
            }
            NOTIFICATION_HIDE_ACTION -> {
                isFloatingBubbleVisible = false
                notificationManager?.notify(NOTIFICATION_ID, createNotificationBuilder().build())
                floatingBubbleWindow?.hideWindow()
            }
            NOTIFICATION_REVEAL_ACTION -> {
                isFloatingBubbleVisible = true
                notificationManager?.notify(NOTIFICATION_ID, createNotificationBuilder().build())
                floatingBubbleWindow?.revealWindow()
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        FloatingAddWordWindowReceiver.closeWindow(this)
        floatingBubbleWindow?.closeWindow()
        floatingBubbleWindow = null

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.edit {
            putBoolean(PREFERENCE_FLOATING_BUBBLE_KEY, false)
        }

        super.onDestroy()
    }

    private fun createNotificationBuilder(): NotificationCompat.Builder {
        // Create notification channel
        val name = "Floating Bubble Channel"
        val descriptionText = "Channel for adjusting floating bubble"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        notificationManager?.createNotificationChannel(channel)

        val hideServiceIntent = Intent(this, NotificationFloatingBubbleService::class.java)
        hideServiceIntent.action = if (isFloatingBubbleVisible) NOTIFICATION_HIDE_ACTION else NOTIFICATION_REVEAL_ACTION
        val hideServicePendingIntent = PendingIntent.getService(
            this,
            SERVICE_HIDE_SHOW_FLOATING_BUBBLE_REQUEST_CODE,
            hideServiceIntent,
            PendingIntent.FLAG_IMMUTABLE
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
            FloatingAddWordWindowReceiver.openWindowPendingIntent(this, null)

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
                if (isFloatingBubbleVisible) "Hide floating bubble" else "Show floating bubble",
                hideServicePendingIntent
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
        private var isFloatingBubbleVisible = false

        fun startService(context: Context) {
            val serviceIntent = Intent(context, NotificationFloatingBubbleService::class.java)
            serviceIntent.action = NOTIFICATION_START_ACTION
            context.startForegroundService(serviceIntent)
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