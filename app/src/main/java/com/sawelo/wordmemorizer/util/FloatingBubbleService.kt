package com.sawelo.wordmemorizer.util

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.util.NotificationUtils.NOTIFICATION_START_ACTION

class FloatingBubbleService : Service() {

    private var floatingBubbleReceiver: FloatingBubbleReceiver? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @SuppressLint("LaunchActivityFromNotification")
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.action == null || intent.action != NOTIFICATION_START_ACTION) {
            stopSelf()
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel
        val name = "Floating Bubble Channel"
        val descriptionText = "Channel for adjusting floating bubble"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        notificationManager.createNotificationChannel(channel)

        floatingBubbleReceiver = FloatingBubbleReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(NOTIFICATION_START_ACTION)
        registerReceiver(floatingBubbleReceiver, intentFilter)

        val receiverIntent = Intent()
        receiverIntent.action = NOTIFICATION_START_ACTION
        val receiverPendingIntent = PendingIntent.getBroadcast(
            this, 0, receiverIntent, PendingIntent.FLAG_IMMUTABLE
        )

        // Create notification builder
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Floating bubble")
            .setContentText("Press to show/hide the bubble")
            .setContentIntent(receiverPendingIntent)

        startForeground(NOTIFICATION_ID, builder.build())

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        if (floatingBubbleReceiver != null) {
            unregisterReceiver(floatingBubbleReceiver)
            floatingBubbleReceiver = null
        }
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "FLOATING_BUBBLE_CHANNEL_ID"
        private const val NOTIFICATION_ID = 1


    }
}