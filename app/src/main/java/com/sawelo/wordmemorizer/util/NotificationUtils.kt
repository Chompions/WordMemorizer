package com.sawelo.wordmemorizer.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.sawelo.wordmemorizer.R

object NotificationUtils {
    private var notificationRemoveBroadcastReceiver: NotificationRemoveBroadcastReceiver? = null

    private fun NotificationManager.createFloatingBubbleNotificationChannel() {
        val name = "Floating Bubble Channel"
        val descriptionText = "Channel for adjusting floating bubble"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        createNotificationChannel(channel)
    }

    private fun NotificationManager.sendFloatingBubbleNotification(context: Context) {
        val deleteIntent = Intent()
        deleteIntent.action = NOTIFICATION_DELETE_ACTION
        val deletePendingIntent = PendingIntent.getBroadcast(
            context, 0, deleteIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        notificationRemoveBroadcastReceiver = NotificationRemoveBroadcastReceiver()
        context.registerReceiver(
            notificationRemoveBroadcastReceiver, IntentFilter(NOTIFICATION_DELETE_ACTION)
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("WordMemorizer floating bubble")
            .setContentText("Displaying floating bubble to add or search words")
            .setDeleteIntent(deletePendingIntent)
        notify(NOTIFICATION_ID, builder.build())
    }

    private fun SharedPreferences.postNotificationPermissionLauncher(activity: AppCompatActivity) =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                edit {
                    putBoolean(PREFERENCE_FLOATING_BUBBLE_KEY, true)
                }
            }
        }

    fun NotificationManager.cancelFloatingBubbleNotification(context: Context) {
        if (notificationRemoveBroadcastReceiver != null) {
            context.unregisterReceiver(notificationRemoveBroadcastReceiver)
            notificationRemoveBroadcastReceiver = null
        }
        cancel(NOTIFICATION_ID)
    }

    fun SharedPreferences.checkPermissionAndSendNotification(activity: AppCompatActivity) {
        val notificationManager = ContextCompat.getSystemService(
            activity, NotificationManager::class.java
        ) as NotificationManager

        if (getBoolean(PREFERENCE_FLOATING_BUBBLE_KEY, false)) {
            if (Build.VERSION.SDK_INT >= 33) {
                if (ActivityCompat.checkSelfPermission(
                        activity,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(
                        activity,
                        "Notification permission required",
                        Toast.LENGTH_SHORT
                    ).show()
                    edit {
                        putBoolean(PREFERENCE_FLOATING_BUBBLE_KEY, false)
                    }
                    postNotificationPermissionLauncher(activity)
                        .launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            notificationManager.createFloatingBubbleNotificationChannel()
            notificationManager.sendFloatingBubbleNotification(activity)
        } else {
            notificationManager.cancelFloatingBubbleNotification(activity)
        }
    }

    const val NOTIFICATION_DELETE_ACTION = "NOTIFICATION_DELETE_ACTION"
    const val PREFERENCE_FLOATING_BUBBLE_KEY = "preference_floating_bubble_key"
    private const val CHANNEL_ID = "FLOATING_BUBBLE_CHANNEL_ID"
    private const val NOTIFICATION_ID = 1
}