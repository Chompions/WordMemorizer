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

object NotificationUtils {

    private fun FragmentActivity.showToastAndChangePref(
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
        permissionLauncher: () -> Unit,
    ) {
        val serviceIntent = Intent(this, FloatingBubbleService::class.java)
        if (sharedPreferences.getBoolean(PREFERENCE_FLOATING_BUBBLE_KEY, false)) {
            if (Build.VERSION.SDK_INT >= 33) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    showToastAndChangePref("Notification permission required", sharedPreferences)
                    permissionLauncher.invoke()
                }
            }
            if (!Settings.canDrawOverlays(this)) {
                showToastAndChangePref("Overlay permission required", sharedPreferences)
                val overlayIntent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(overlayIntent)
            } else {
                serviceIntent.action = NOTIFICATION_START_ACTION
                startForegroundService(serviceIntent)
            }
        } else {
            stopService(serviceIntent)
        }
    }

    const val NOTIFICATION_START_ACTION = "NOTIFICATION_START_ACTION"
    const val NOTIFICATION_STOP_ACTION = "NOTIFICATION_STOP_ACTION"

    const val PREFERENCE_FLOATING_BUBBLE_KEY = "preference_floating_bubble_key"
}