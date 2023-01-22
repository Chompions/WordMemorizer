package com.sawelo.wordmemorizer.util

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.sawelo.wordmemorizer.util.NotificationUtils.cancelFloatingBubbleNotification

class NotificationRemoveBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == NotificationUtils.NOTIFICATION_DELETE_ACTION ) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            sharedPreferences.edit {
                putBoolean(NotificationUtils.PREFERENCE_FLOATING_BUBBLE_KEY, false)
            }

            val notificationManager = ContextCompat.getSystemService(
                context, NotificationManager::class.java
            ) as NotificationManager
            notificationManager.cancelFloatingBubbleNotification(context)
        }
    }
}