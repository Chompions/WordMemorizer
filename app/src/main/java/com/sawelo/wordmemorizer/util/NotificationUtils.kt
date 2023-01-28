package com.sawelo.wordmemorizer.util

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.fragment.app.FragmentActivity
import com.sawelo.wordmemorizer.service.FloatingBubbleService
import com.sawelo.wordmemorizer.util.Constants.NOTIFICATION_START_ACTION
import com.sawelo.wordmemorizer.util.Constants.NOTIFICATION_STOP_ACTION
import com.sawelo.wordmemorizer.util.Constants.PREFERENCE_FLOATING_BUBBLE_KEY

object NotificationUtils {

    private fun FragmentActivity.showToastAndCancelPref(
        text: String,
        sharedPreferences: SharedPreferences
    ) {
        Toast.makeText(
            this,
            text,
            Toast.LENGTH_SHORT
        ).show()
        sharedPreferences.edit {
            putBoolean(PREFERENCE_FLOATING_BUBBLE_KEY, false)
        }
    }

    fun FragmentActivity.checkPermissionAndStartFloatingBubbleService(
        sharedPreferences: SharedPreferences,
        notificationPermissionLauncher: () -> Unit,
    ) {
        if (sharedPreferences.getBoolean(PREFERENCE_FLOATING_BUBBLE_KEY, false)) {
            if (Build.VERSION.SDK_INT >= 33) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    showToastAndCancelPref("Notification permission required", sharedPreferences)
                    notificationPermissionLauncher.invoke()
                }
            }
            if (!Settings.canDrawOverlays(this)) {
                showToastAndCancelPref("Overlay permission required", sharedPreferences)
                val overlayIntent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(overlayIntent)
            } else {
                sharedPreferences.edit {
                    putBoolean(PREFERENCE_FLOATING_BUBBLE_KEY, true)
                }

                val serviceIntent = Intent(this, FloatingBubbleService::class.java)
                serviceIntent.action = NOTIFICATION_START_ACTION
                startForegroundService(serviceIntent)
            }
        } else {
            val serviceIntent = Intent(this, FloatingBubbleService::class.java)
            serviceIntent.action = NOTIFICATION_STOP_ACTION
            stopService(serviceIntent)
        }
    }
}