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
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier
import com.sawelo.wordmemorizer.activity.SettingsActivity
import com.sawelo.wordmemorizer.service.DownloadService
import com.sawelo.wordmemorizer.service.NotificationFloatingBubbleService
import com.sawelo.wordmemorizer.util.Constants.PREFERENCE_DRAW_CHARACTER_KEY
import com.sawelo.wordmemorizer.util.Constants.PREFERENCE_FLOATING_BUBBLE_KEY
import com.sawelo.wordmemorizer.util.Constants.PREFERENCE_OFFLINE_TRANSLATION_KEY
import com.sawelo.wordmemorizer.util.ViewUtils.showToast

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

    fun checkAllSettings() {
        checkPermissionForFloatingBubble()
        checkDownloadForDrawDigitalInk()
        checkDownloadForTranslator()
    }

    fun checkDownloadForTranslator() {
        val currentPreference =
            sharedPreferences?.getBoolean(PREFERENCE_OFFLINE_TRANSLATION_KEY, false)
        if (currentPreference == true) {
            Constants.isOfflineTranslationDownloadingFlow.value = true
            val japaneseModel = TranslateRemoteModel.Builder(TranslateLanguage.JAPANESE).build()
            remoteModelManager.isModelDownloaded(japaneseModel).addOnSuccessListener { isDownloaded ->
                if (!isDownloaded) {
                    DownloadService.startDownloadTranslatorService(activity)
                } else {
                    Constants.isOfflineTranslationDownloadingFlow.value = false
                }
            }
        }
    }

    fun checkDownloadForDrawDigitalInk() {
        val currentPreference =
            sharedPreferences?.getBoolean(PREFERENCE_DRAW_CHARACTER_KEY, false)
        if (currentPreference == true) {
            Constants.isDrawDigitalInkDownloadingFlow.value = true
            val modelIdentifier = DigitalInkRecognitionModelIdentifier.fromLanguageTag("ja-JP")
            val model = DigitalInkRecognitionModel.builder(modelIdentifier!!).build()
            remoteModelManager.isModelDownloaded(model).addOnSuccessListener { isDownloaded ->
                if (!isDownloaded) {
                    DownloadService.startDownloadDrawDigitalInkService(activity)
                } else {
                    Constants.isDrawDigitalInkDownloadingFlow.value = false
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
                    sharedPreferences?.edit {
                        putBoolean(PREFERENCE_FLOATING_BUBBLE_KEY, false)
                    }
                    activity.showToast("Notification permission required")
                    postNotificationPermissionLauncher.launch(
                        android.Manifest.permission.POST_NOTIFICATIONS
                    )
                }
            }
            if (!Settings.canDrawOverlays(activity)) {
                sharedPreferences?.edit {
                    putBoolean(PREFERENCE_FLOATING_BUBBLE_KEY, false)
                }
                activity.showToast("Draw overlay permission required")
                val overlayIntent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${Constants.PACKAGE_NAME}")
                )
                activity.startActivity(overlayIntent)
            } else {
                sharedPreferences?.edit {
                    putBoolean(PREFERENCE_FLOATING_BUBBLE_KEY, true)
                }
                NotificationFloatingBubbleService.startService(activity)
            }
        } else {
            NotificationFloatingBubbleService.stopService(activity)
        }
    }

}