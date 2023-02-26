package com.sawelo.wordmemorizer.service.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.sawelo.wordmemorizer.MainApplication
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.data.preferences.window.FloatingBubbleProcess
import com.sawelo.wordmemorizer.data.repository.PreferenceRepository
import com.sawelo.wordmemorizer.util.Constants

class FloatingBubbleNotification(
    private val lifecycleService: LifecycleService,
    private val preferenceRepository: PreferenceRepository
) {
    private val notificationManager = lifecycleService.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager

    private fun openDialogPendingIntent(): PendingIntent {
        val receiverIntent = Intent()
        receiverIntent.action = Constants.RECEIVER_OPEN_ACTION
        receiverIntent.`package` = MainApplication.PACKAGE_NAME
        return PendingIntent.getBroadcast(
            lifecycleService,
            Constants.RECEIVER_OPEN_FLOATING_DIALOG_REQUEST_CODE,
            receiverIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun wrapServicePendingIntent(isFloatingBubbleUnwrapped: Boolean): PendingIntent {
        val wrapServiceIntent = Intent(lifecycleService, NotificationFloatingBubbleService::class.java)
        wrapServiceIntent.action = if (isFloatingBubbleUnwrapped)
            Constants.NOTIFICATION_WRAP_ACTION else Constants.NOTIFICATION_UNWRAP_ACTION
        wrapServiceIntent.putExtra(IS_SENT_FROM_NOTIF, true)
        return PendingIntent.getService(
            lifecycleService,
            Constants.SERVICE_WRAP_FLOATING_BUBBLE_REQUEST_CODE,
            wrapServiceIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun stopServicePendingIntent(): PendingIntent {
        val stopServiceIntent = Intent(lifecycleService, NotificationFloatingBubbleService::class.java)
        stopServiceIntent.action = Constants.NOTIFICATION_STOP_ACTION
        return PendingIntent.getService(
            lifecycleService,
            Constants.SERVICE_CLOSE_FLOATING_BUBBLE_REQUEST_CODE,
            stopServiceIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createChannel() {
        // Create notification channel
        val name = "Floating Bubble Channel"
        val descriptionText = "Channel for adjusting floating bubble"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotificationBuilder(): Notification {
        createChannel()

        val isFloatingBubbleUnwrapped =
            preferenceRepository.getCurrentProcessSnapshot(FloatingBubbleProcess.IsUnwrapped)
        val largeIcon = BitmapFactory.decodeResource(lifecycleService.resources, R.drawable.ic_stat_name)

        // Create notification builder
        return NotificationCompat.Builder(lifecycleService, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setLargeIcon(largeIcon)
            .setContentTitle("Tap to add word")
            .setContentText("Tap here to open the add dialog")
            .setShowWhen(false)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setContentIntent(openDialogPendingIntent())
            .addAction(
                R.drawable.baseline_visibility_24,
                if (isFloatingBubbleUnwrapped)
                    "Hide floating bubble" else "Show floating bubble",
                wrapServicePendingIntent(isFloatingBubbleUnwrapped)
            )
            .addAction(
                R.drawable.ic_baseline_close_24,
                "Turn off",
                stopServicePendingIntent()
            )
            .build()
    }

    fun notifyNotification() {
        notificationManager.notify(NOTIFICATION_ID, createNotificationBuilder())
    }

    fun startForegroundNotification() {
        lifecycleService.startForeground(NOTIFICATION_ID, createNotificationBuilder())
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "FLOATING_BUBBLE_CHANNEL_ID"
        const val IS_SENT_FROM_NOTIF = "IS_SENT_FROM_NOTIF"
    }
}