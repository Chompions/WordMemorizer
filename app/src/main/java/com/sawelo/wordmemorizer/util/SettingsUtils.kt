package com.sawelo.wordmemorizer.util

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier
import com.sawelo.wordmemorizer.activity.SettingsActivity
import com.sawelo.wordmemorizer.service.FloatingBubbleService
import com.sawelo.wordmemorizer.util.Constants.PREFERENCE_DRAW_CHARACTER_KEY
import com.sawelo.wordmemorizer.util.Constants.PREFERENCE_FLOATING_BUBBLE_KEY
import com.sawelo.wordmemorizer.util.Constants.PREFERENCE_OFFLINE_TRANSLATION_KEY
import com.sawelo.wordmemorizer.util.WordUtils.showToast

class SettingsUtils(private val activity: FragmentActivity) {
    private var sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
    private val remoteModelManager = RemoteModelManager.getInstance()

    private val postNotificationPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted && activity is SettingsActivity) {
            activity.finish()
        }
    }

    private fun editPrefWithToast(key: String, boolean: Boolean, toast: String,) {
        sharedPreferences?.edit {
            putBoolean(key, boolean)
        }
        activity.showToast(toast)
    }

    fun checkAll() {
        checkPermissionForFloatingBubble()
        checkDownloadForDrawDigitalInk()
        checkDownloadForTranslator()
    }

    fun checkDownloadForTranslator() {
        val currentPreference =
            sharedPreferences?.getBoolean(PREFERENCE_OFFLINE_TRANSLATION_KEY, false)
        if (currentPreference == true) {
            val japaneseModel = TranslateRemoteModel.Builder(TranslateLanguage.JAPANESE).build()
            remoteModelManager.isModelDownloaded(japaneseModel).addOnSuccessListener { isDownloaded ->
                if (!isDownloaded) {
                    editPrefWithToast(
                        PREFERENCE_OFFLINE_TRANSLATION_KEY,
                        false,
                        "Downloading Japanese translator, please wait")
                    val options = TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.JAPANESE)
                        .setTargetLanguage(TranslateLanguage.ENGLISH)
                        .build()
                    val japaneseEnglishTranslator = Translation.getClient(options)
                    japaneseEnglishTranslator.downloadModelIfNeeded()
                        .addOnFailureListener {
                            editPrefWithToast(
                                PREFERENCE_OFFLINE_TRANSLATION_KEY,
                                false,
                                "Download failed, please try again")
                            japaneseEnglishTranslator.close()
                        }
                        .addOnSuccessListener {
                            editPrefWithToast(
                                PREFERENCE_OFFLINE_TRANSLATION_KEY,
                                true,
                                "Download complete")
                            japaneseEnglishTranslator.close()
                        }
                }
            }
        }
    }

    fun checkDownloadForDrawDigitalInk() {
        val currentPreference =
            sharedPreferences?.getBoolean(PREFERENCE_DRAW_CHARACTER_KEY, false)
        if (currentPreference == true) {
            val modelIdentifier = DigitalInkRecognitionModelIdentifier.fromLanguageTag("ja-JP")
            val model = DigitalInkRecognitionModel.builder(modelIdentifier!!).build()
            remoteModelManager.isModelDownloaded(model).addOnSuccessListener { isDownloaded ->
                if (!isDownloaded) {
                    editPrefWithToast(
                        PREFERENCE_DRAW_CHARACTER_KEY,
                        false,
                        "Downloading character recognizer, please wait")
                    remoteModelManager.download(model, DownloadConditions.Builder().build())
                        .addOnFailureListener {
                            editPrefWithToast(
                                PREFERENCE_DRAW_CHARACTER_KEY,
                                false,
                                "Download failed, please try again")
                        }
                        .addOnSuccessListener {
                            editPrefWithToast(
                                PREFERENCE_DRAW_CHARACTER_KEY,
                                true,
                                "Download complete")
                        }
                }
            }
        }
    }

    fun checkPermissionForFloatingBubble() {
        val currentPreference =
            sharedPreferences?.getBoolean(PREFERENCE_FLOATING_BUBBLE_KEY, false)
        if (currentPreference == true) {
            if (Build.VERSION.SDK_INT >= 33) {
                if (ActivityCompat.checkSelfPermission(
                        activity,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    editPrefWithToast(
                        PREFERENCE_FLOATING_BUBBLE_KEY,
                        false,
                        "Notification permission required")
                    postNotificationPermissionLauncher.launch(
                        android.Manifest.permission.POST_NOTIFICATIONS
                    )
                }
            }
            if (!Settings.canDrawOverlays(activity)) {
                editPrefWithToast(
                    PREFERENCE_FLOATING_BUBBLE_KEY,
                    false,
                    "Draw overlay permission required")
                val overlayIntent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${Constants.PACKAGE_NAME}")
                )
                activity.startActivity(overlayIntent)
            } else {
                sharedPreferences?.edit {
                    putBoolean(PREFERENCE_FLOATING_BUBBLE_KEY, true)
                }
                val serviceIntent = Intent(activity, FloatingBubbleService::class.java)
                serviceIntent.action = Constants.NOTIFICATION_START_ACTION
                activity.startForegroundService(serviceIntent)
            }
        } else {
            val serviceIntent = Intent(activity, FloatingBubbleService::class.java)
            serviceIntent.action = Constants.NOTIFICATION_STOP_ACTION
            activity.stopService(serviceIntent)
        }
    }

}