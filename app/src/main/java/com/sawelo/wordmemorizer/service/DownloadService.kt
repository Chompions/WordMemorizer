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
import androidx.preference.PreferenceManager
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.util.Constants
import com.sawelo.wordmemorizer.util.Constants.NOTIFICATION_DOWNLOAD_DRAW_DIGITAL_INK
import com.sawelo.wordmemorizer.util.Constants.NOTIFICATION_DOWNLOAD_EXTRA
import com.sawelo.wordmemorizer.util.Constants.NOTIFICATION_DOWNLOAD_TRANSLATOR
import com.sawelo.wordmemorizer.util.Constants.NOTIFICATION_START_ACTION
import com.sawelo.wordmemorizer.util.ViewUtils.showToast

class DownloadService: Service() {

    private var sharedPreferences: SharedPreferences? = null
    private var notificationManager: NotificationManager? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        when (intent?.action) {
            NOTIFICATION_START_ACTION -> {
                createGeneralNotification()
                when (intent.getStringExtra(NOTIFICATION_DOWNLOAD_EXTRA)) {
                    NOTIFICATION_DOWNLOAD_TRANSLATOR -> createTranslatorNotification()
                    NOTIFICATION_DOWNLOAD_DRAW_DIGITAL_INK -> createDrawDigitalInkNotification()
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        sharedPreferences = null
        notificationManager = null
    }

    private fun createGeneralNotification() {
        createChannel()
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Adding features")
            .setContentText("Downloading additional features")
        startForeground(NOTIFICATION_GENERAL_ID, builder.build())
    }

    private fun createTranslatorNotification() {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Downloading offline translation")
        notificationManager?.notify(NOTIFICATION_TRANSLATOR_ID, builder.build())
        downloadForTranslator()
    }

    private fun createDrawDigitalInkNotification() {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Downloading character recognition")
        notificationManager?.notify(NOTIFICATION_DRAW_DIGITAL_INK_ID, builder.build())
        downloadForDrawDigitalInk()
    }


    private fun downloadForTranslator() {
        editPrefWithToast(
            Constants.PREFERENCE_OFFLINE_TRANSLATION_KEY,
            "Downloading Japanese translator, please wait")
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.JAPANESE)
            .setTargetLanguage(TranslateLanguage.ENGLISH)
            .build()
        val japaneseEnglishTranslator = Translation.getClient(options)
        japaneseEnglishTranslator.downloadModelIfNeeded()
            .addOnFailureListener {
                editPrefWithToast(
                    Constants.PREFERENCE_OFFLINE_TRANSLATION_KEY,
                    "Download failed: ${it.message}")
            }
            .addOnSuccessListener {
                editPrefWithToast(
                    Constants.PREFERENCE_OFFLINE_TRANSLATION_KEY,
                    "Download complete")
            }
            .addOnCanceledListener {
                Constants.isOfflineTranslationDownloadingFlow.value = false
                japaneseEnglishTranslator.close()
                notificationManager?.cancel(NOTIFICATION_TRANSLATOR_ID)
                stopDownloadTranslatorService(this)
            }
            .addOnCompleteListener {
                Constants.isOfflineTranslationDownloadingFlow.value = false
                japaneseEnglishTranslator.close()
                notificationManager?.cancel(NOTIFICATION_TRANSLATOR_ID)
                stopDownloadTranslatorService(this)
            }
    }

    private fun downloadForDrawDigitalInk() {
        editPrefWithToast(
            Constants.PREFERENCE_DRAW_CHARACTER_KEY,
            "Downloading character recognizer, please wait")
        val modelIdentifier = DigitalInkRecognitionModelIdentifier.fromLanguageTag("ja-JP")
        val model = DigitalInkRecognitionModel.builder(modelIdentifier!!).build()
        RemoteModelManager.getInstance().download(model, DownloadConditions.Builder().build())
            .addOnFailureListener {
                editPrefWithToast(
                    Constants.PREFERENCE_DRAW_CHARACTER_KEY,
                    "Download failed: ${it.message}")
            }
            .addOnSuccessListener {
                editPrefWithToast(
                    Constants.PREFERENCE_DRAW_CHARACTER_KEY,
                    "Download complete")
            }
            .addOnCanceledListener {
                Constants.isDrawDigitalInkDownloadingFlow.value = false
                notificationManager?.cancel(NOTIFICATION_DRAW_DIGITAL_INK_ID)
                stopDownloadDrawDigitalInkService(this)
            }
            .addOnCompleteListener {
                Constants.isDrawDigitalInkDownloadingFlow.value = false
                notificationManager?.cancel(NOTIFICATION_DRAW_DIGITAL_INK_ID)
                stopDownloadDrawDigitalInkService(this)
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

    private fun editPrefWithToast(key: String, toast: String,) {
        sharedPreferences?.edit {
            putBoolean(key, false)
        }
        showToast(toast)
    }

    companion object {
        private const val CHANNEL_ID = "DOWNLOAD_CHANNEL_ID"
        private const val NOTIFICATION_GENERAL_ID = 20
        private const val NOTIFICATION_TRANSLATOR_ID = 21
        private const val NOTIFICATION_DRAW_DIGITAL_INK_ID = 22

        private var translatorIntent: Intent? = null
        private var drawDigitalInkIntent: Intent? = null

        fun startDownloadTranslatorService(context: Context) {
            translatorIntent = Intent(context, DownloadService::class.java)
            translatorIntent?.action = NOTIFICATION_START_ACTION
            translatorIntent?.putExtra(NOTIFICATION_DOWNLOAD_EXTRA, NOTIFICATION_DOWNLOAD_TRANSLATOR)
            context.startForegroundService(translatorIntent)
        }

        fun startDownloadDrawDigitalInkService(context: Context) {
            drawDigitalInkIntent = Intent(context, DownloadService::class.java)
            drawDigitalInkIntent?.action = NOTIFICATION_START_ACTION
            drawDigitalInkIntent?.putExtra(NOTIFICATION_DOWNLOAD_EXTRA, NOTIFICATION_DOWNLOAD_DRAW_DIGITAL_INK)
            context.startForegroundService(drawDigitalInkIntent)
        }

        fun stopDownloadTranslatorService(context: Context) {
            context.stopService(translatorIntent)
        }

        fun stopDownloadDrawDigitalInkService(context: Context) {
            context.stopService(drawDigitalInkIntent)
        }
    }
}