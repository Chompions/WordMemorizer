package com.sawelo.wordmemorizer.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.dataStore
import com.sawelo.wordmemorizer.util.Constants.NOTIFICATION_START_ACTION
import com.sawelo.wordmemorizer.util.enum_class.SettingsProcess
import com.sawelo.wordmemorizer.util.enum_class.SettingsSwitch
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseDownloadService(
    private val notificationId: Int,
    private val notificationText: String,
) : Service() {
    @Inject
    lateinit var sharedPreferences: SharedPreferences
    private var notificationManager: NotificationManager? = null

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        when (intent?.action) {
            NOTIFICATION_START_ACTION -> {
                createChannel()
                startDownload()
                downloadProcess()
            }
            else -> stopSelf()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationManager = null
    }

    private fun createChannel() {
        // Create notification channel
        val name = "Download Channel"
        val descriptionText = "Channel for displaying download info"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        notificationManager?.createNotificationChannel(channel)
    }

    private fun startDownload() {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Adding features")
            .setContentText(notificationText)
            .setOngoing(true)
        startForeground(notificationId, builder.build())
    }

    abstract fun downloadProcess()

    fun SettingsProcess.setCurrentProcess(boolean: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            applicationContext.dataStore.edit { preferences ->
                preferences[booleanPreferencesKey(processKey)] = boolean
            }
        }
    }

    var SettingsSwitch.isChecked: Boolean
        get() = sharedPreferences.getBoolean(switchKey, false)
        set(value) = sharedPreferences.edit { putBoolean(switchKey, value) }

    companion object {
        private const val CHANNEL_ID = "DOWNLOAD_CHANNEL_ID"
    }
}