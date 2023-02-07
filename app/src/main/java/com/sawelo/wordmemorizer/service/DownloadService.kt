package com.sawelo.wordmemorizer.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.dataStore
import com.sawelo.wordmemorizer.fragment.SettingsSwitch
import com.sawelo.wordmemorizer.util.Constants.NOTIFICATION_DOWNLOAD_DRAW_DIGITAL_INK
import com.sawelo.wordmemorizer.util.Constants.NOTIFICATION_DOWNLOAD_EXTRA
import com.sawelo.wordmemorizer.util.Constants.NOTIFICATION_DOWNLOAD_TRANSLATOR
import com.sawelo.wordmemorizer.util.Constants.NOTIFICATION_START_ACTION
import com.sawelo.wordmemorizer.util.SettingsProcess
import com.sawelo.wordmemorizer.util.ViewUtils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DownloadService : Service() {
    private var notificationManager: NotificationManager? = null
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        when (intent?.action) {
            NOTIFICATION_START_ACTION -> {
                when (intent.getStringExtra(NOTIFICATION_DOWNLOAD_EXTRA)) {
                    NOTIFICATION_DOWNLOAD_TRANSLATOR -> createTranslatorNotification()
                    NOTIFICATION_DOWNLOAD_DRAW_DIGITAL_INK -> createDrawDigitalInkNotification()
                }
            }
            else -> stopSelf()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationManager = null
    }

    private fun createTranslatorNotification() {
        createChannel()
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Adding features")
            .setContentText("Downloading offline translation")
        startForeground(NOTIFICATION_TRANSLATOR_ID, builder.build())
        downloadForTranslator()
    }

    private fun createDrawDigitalInkNotification() {
        createChannel()
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Adding features")
            .setContentText("Downloading character recognition")
        startForeground(NOTIFICATION_DRAW_DIGITAL_INK_ID, builder.build())
        downloadForDrawDigitalInk()
    }

    private fun downloadForTranslator() {
        showToast("Downloading Japanese translator, please wait")
        SettingsSwitch.TranslationSwitch.isChecked = false
        SettingsProcess.TranslationDownload.setCurrentProcess(true)
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.JAPANESE)
            .setTargetLanguage(TranslateLanguage.ENGLISH)
            .build()
        val japaneseEnglishTranslator = Translation.getClient(options)
        japaneseEnglishTranslator.downloadModelIfNeeded()
            .addOnFailureListener {
                showToast("Download failed: ${it.message}")
                SettingsSwitch.TranslationSwitch.isChecked = false
                Log.e("DownloadService", "Download failed: ${it.message}")
            }
            .addOnSuccessListener {
                showToast("Download complete")
                SettingsSwitch.TranslationSwitch.isChecked = true
            }
            .addOnCanceledListener {
                SettingsProcess.TranslationDownload.setCurrentProcess(false)
                japaneseEnglishTranslator.close()
                stopSelf()
            }
            .addOnCompleteListener {
                SettingsProcess.TranslationDownload.setCurrentProcess(false)
                japaneseEnglishTranslator.close()
                stopSelf()
            }
    }



    private fun downloadForDrawDigitalInk() {
        showToast("Downloading character recognizer, please wait")
        SettingsSwitch.DrawSwitch.isChecked = false
        SettingsProcess.DrawDownload.setCurrentProcess(true)
        val modelIdentifier = DigitalInkRecognitionModelIdentifier.fromLanguageTag("ja-JP")
        val model = DigitalInkRecognitionModel.builder(modelIdentifier!!).build()
        RemoteModelManager.getInstance().download(model, DownloadConditions.Builder().build())
            .addOnFailureListener {
                showToast("Download failed: ${it.message}")
                SettingsSwitch.DrawSwitch.isChecked = false
                Log.e("DownloadService", "Download failed: ${it.message}")
            }
            .addOnSuccessListener {
                showToast("Download complete")
                SettingsSwitch.DrawSwitch.isChecked = true
            }
            .addOnCanceledListener {
                SettingsProcess.DrawDownload.setCurrentProcess(false)
                stopSelf()
            }
            .addOnCompleteListener {
                SettingsProcess.DrawDownload.setCurrentProcess(false)
                stopSelf()
            }
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

    private fun SettingsProcess.setCurrentProcess(boolean: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            applicationContext.dataStore.edit { preferences ->
                preferences[booleanPreferencesKey(processKey)] = boolean
            }
        }
    }

    private var SettingsSwitch.isChecked: Boolean
        get() = sharedPreferences.getBoolean(switchKey, false)
        set(value) = sharedPreferences.edit { putBoolean(switchKey, value) }

    companion object {
        private const val CHANNEL_ID = "DOWNLOAD_CHANNEL_ID"
        private const val NOTIFICATION_TRANSLATOR_ID = 21
        private const val NOTIFICATION_DRAW_DIGITAL_INK_ID = 22

        private var translatorIntent: Intent? = null
        private var drawDigitalInkIntent: Intent? = null

        fun startDownloadTranslatorService(context: Context) {
            translatorIntent = Intent(context, DownloadService::class.java)
            translatorIntent?.action = NOTIFICATION_START_ACTION
            translatorIntent?.putExtra(
                NOTIFICATION_DOWNLOAD_EXTRA,
                NOTIFICATION_DOWNLOAD_TRANSLATOR
            )
            context.startForegroundService(translatorIntent)
        }

        fun startDownloadDrawDigitalInkService(context: Context) {
            drawDigitalInkIntent = Intent(context, DownloadService::class.java)
            drawDigitalInkIntent?.action = NOTIFICATION_START_ACTION
            drawDigitalInkIntent?.putExtra(
                NOTIFICATION_DOWNLOAD_EXTRA,
                NOTIFICATION_DOWNLOAD_DRAW_DIGITAL_INK
            )
            context.startForegroundService(drawDigitalInkIntent)
        }
    }
}