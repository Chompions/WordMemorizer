package com.sawelo.wordmemorizer.util

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier
import com.sawelo.wordmemorizer.MainApplication
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.dataStore
import com.sawelo.wordmemorizer.service.DownloadDrawService
import com.sawelo.wordmemorizer.service.DownloadTranslatorService
import com.sawelo.wordmemorizer.service.NotificationFloatingBubbleService
import com.sawelo.wordmemorizer.util.enum_class.SettingsProcess
import com.sawelo.wordmemorizer.util.enum_class.SettingsSwitch
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@ActivityScoped
class SettingsUtils @Inject constructor(
    @ActivityContext activityContext: Context,
    private val sharedPreferences: SharedPreferences
) {
    private val activity = activityContext as FragmentActivity
    private val remoteModelManager = RemoteModelManager.getInstance()

    suspend fun checkAllSettings() {
        checkPermissionForFloatingBubble()
        checkDownloadForDrawDigitalInk()
        checkDownloadForTranslator()
    }

    suspend fun checkDownloadForTranslator() {
        if (SettingsSwitch.TranslationSwitch.isChecked &&
            !SettingsProcess.PreparingDownloadProcess.getCurrentProcess()
        ) {
            SettingsProcess.PreparingDownloadProcess.setCurrentProcess(true)
            val japaneseModel = TranslateRemoteModel.Builder(TranslateLanguage.JAPANESE).build()
            remoteModelManager.isModelDownloaded(japaneseModel)
                .addOnSuccessListener { isDownloaded ->
                    if (!isDownloaded) {
                        createDialog(
                            "This feature will need to download additional files, " +
                                    "this is a one-time download, and it won't take long. " +
                                    "\nDownload size approximately ±70MB"
                        ) {
                            if (it) {
                                SettingsProcess.PreparingDownloadProcess.setCurrentProcess(false)
                                DownloadTranslatorService.startService(activity)
                            }
                            else {
                                SettingsSwitch.TranslationSwitch.isChecked = false
                                SettingsProcess.PreparingDownloadProcess.setCurrentProcess(false)
                            }
                        }
                    } else {
                        SettingsProcess.PreparingDownloadProcess.setCurrentProcess(false)
                    }
                }
                .addOnFailureListener {
                    SettingsSwitch.TranslationSwitch.isChecked = false
                    SettingsProcess.PreparingDownloadProcess.setCurrentProcess(false)
                }
        }
    }

    suspend fun checkDownloadForDrawDigitalInk() {
        if (SettingsSwitch.DrawSwitch.isChecked &&
            !SettingsProcess.PreparingDownloadProcess.getCurrentProcess()
        ) {
            SettingsProcess.PreparingDownloadProcess.setCurrentProcess(true)
            val modelIdentifier = DigitalInkRecognitionModelIdentifier.fromLanguageTag("ja-JP")
            val model = DigitalInkRecognitionModel.builder(modelIdentifier!!).build()
            remoteModelManager.isModelDownloaded(model).addOnSuccessListener { isDownloaded ->
                if (!isDownloaded) {
                    createDialog(
                        "This feature will need to download additional files, " +
                                "this is a one-time download, and it won't take long. " +
                                "\nDownload size approximately ±30MB"
                    ) {
                        if (it) {
                            SettingsProcess.PreparingDownloadProcess.setCurrentProcess(false)
                            DownloadDrawService.startService(activity)
                        }
                        else {
                            SettingsSwitch.DrawSwitch.isChecked = false
                            SettingsProcess.PreparingDownloadProcess.setCurrentProcess(false)
                        }
                    }
                } else {
                    SettingsProcess.PreparingDownloadProcess.setCurrentProcess(false)
                }
            }.addOnFailureListener {
                SettingsSwitch.DrawSwitch.isChecked = false
                SettingsProcess.PreparingDownloadProcess.setCurrentProcess(false)
            }
        }
    }

    suspend fun checkPermissionForFloatingBubble() {
        if (SettingsSwitch.FloatingBubbleSwitch.isChecked &&
            !SettingsProcess.FloatingBubbleSetUp.getCurrentProcess()
        ) {
            SettingsProcess.FloatingBubbleSetUp.setCurrentProcess(true)
            if (Build.VERSION.SDK_INT >= 33) {
                checkPostNotificationForFloatingBubble()
            } else checkDrawOverlayForFloatingBubble()
        } else {
            SettingsProcess.FloatingBubbleSetUp.setCurrentProcess(false)
            NotificationFloatingBubbleService.stopService(activity)
        }
    }

    private val postNotificationPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        SettingsSwitch.FloatingBubbleSwitch.isChecked = false
        SettingsProcess.FloatingBubbleSetUp.setCurrentProcess(false)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkPostNotificationForFloatingBubble() {
        if (ActivityCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            createDialog(
                "Due to Android 13 or later requirements, to make sure this feature run " +
                        "properly, please allow WordMemorizer to post notification"
            ) {
                if (it) postNotificationPermissionLauncher.launch(
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) else {
                    SettingsSwitch.FloatingBubbleSwitch.isChecked = false
                    SettingsProcess.FloatingBubbleSetUp.setCurrentProcess(false)
                }
            }
        } else checkDrawOverlayForFloatingBubble()
    }

    private fun checkDrawOverlayForFloatingBubble() {
        if (!Settings.canDrawOverlays(activity)) {
            createDialog(
                "To make sure floating bubble is visible anywhere, " +
                        "please allow WordMemorizer to display over other apps"
            ) {
                if (it) {
                    val overlayIntent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${MainApplication.PACKAGE_NAME}")
                    )
                    activity.startActivity(overlayIntent)
                    SettingsSwitch.FloatingBubbleSwitch.isChecked = false
                    SettingsProcess.FloatingBubbleSetUp.setCurrentProcess(false)
                }
                SettingsSwitch.FloatingBubbleSwitch.isChecked = false
                SettingsProcess.FloatingBubbleSetUp.setCurrentProcess(false)
            }
        } else {
            SettingsSwitch.FloatingBubbleSwitch.isChecked = true
            SettingsProcess.FloatingBubbleSetUp.setCurrentProcess(false)
            NotificationFloatingBubbleService.startService(activity)
        }
    }

    private suspend fun SettingsProcess.getCurrentProcess(): Boolean {
        val preferences = activity.dataStore.data.first()
        return preferences[booleanPreferencesKey(processKey)] ?: false
    }

    private fun SettingsProcess.setCurrentProcess(boolean: Boolean) {
        activity.lifecycleScope.launch {
            activity.dataStore.edit { preferences ->
                preferences[booleanPreferencesKey(processKey)] = boolean
            }
        }
    }

    private var SettingsSwitch.isChecked: Boolean
        get() = sharedPreferences.getBoolean(switchKey, false)
        set(value) = sharedPreferences.edit(commit = true) { putBoolean(switchKey, value) }

    private fun createDialog(message: String, onClick: (Boolean) -> Unit) {
        val alertDialog = MaterialAlertDialogBuilder(activity).apply {
            setTitle("Feature notice")
            setMessage(message)
            setPositiveButton(R.string.ok) { dialog, _ ->
                dialog.dismiss()
                onClick.invoke(true)
            }
            setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.cancel()
                onClick.invoke(false)
            }
            setOnCancelListener {
                onClick.invoke(false)
            }
        }.create()
        if (!activity.isFinishing) {
            alertDialog.show()
        }
    }

}