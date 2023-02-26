package com.sawelo.wordmemorizer.ui.ui_util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier
import com.sawelo.wordmemorizer.MainApplication
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.data.preferences.settings.SettingsProcess
import com.sawelo.wordmemorizer.data.preferences.settings.SettingsSwitch
import com.sawelo.wordmemorizer.data.repository.PreferenceRepository
import com.sawelo.wordmemorizer.service.download.DownloadDrawService
import com.sawelo.wordmemorizer.service.download.DownloadTranslatorService
import com.sawelo.wordmemorizer.service.notification.NotificationFloatingBubbleService
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class SettingsUtil @Inject constructor(
    @ActivityContext activityContext: Context,
    private val repository: PreferenceRepository,
) {
    private val activity = activityContext as FragmentActivity
    private val remoteModelManager = RemoteModelManager.getInstance()

    fun checkAllSettings() {
        checkPermissionForFloatingBubble()
        checkDownloadForDrawDigitalInk()
        checkDownloadForTranslator()
    }

    fun checkDownloadForTranslator() {
        if (
            repository.getCurrentSwitch(SettingsSwitch.TranslationSwitch) &&
            !repository.getCurrentProcessSnapshot(SettingsProcess.PrepareSetupProcess)
        ) {
            repository.setCurrentProcess(SettingsProcess.PrepareSetupProcess, true)
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
                                repository.setCurrentProcess(SettingsProcess.PrepareSetupProcess, false)
                                DownloadTranslatorService.startService(activity)
                            }
                            else {
                                repository.setCurrentSwitch(SettingsSwitch.TranslationSwitch, false)
                                repository.setCurrentProcess(SettingsProcess.PrepareSetupProcess, false)
                            }
                        }
                    } else {
                        repository.setCurrentProcess(SettingsProcess.PrepareSetupProcess, false)
                    }
                }
                .addOnFailureListener {
                    repository.setCurrentSwitch(SettingsSwitch.TranslationSwitch, false)
                    repository.setCurrentProcess(SettingsProcess.PrepareSetupProcess, false)
                }
        }
    }

    fun checkDownloadForDrawDigitalInk() {
        if (repository.getCurrentSwitch(SettingsSwitch.DrawSwitch) &&
            !repository.getCurrentProcessSnapshot(SettingsProcess.PrepareSetupProcess)
        ) {
            repository.setCurrentProcess(SettingsProcess.PrepareSetupProcess, true)
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
                            repository.setCurrentProcess(SettingsProcess.PrepareSetupProcess, false)
                            DownloadDrawService.startService(activity)
                        }
                        else {
                            repository.setCurrentSwitch(SettingsSwitch.DrawSwitch, false)
                            repository.setCurrentProcess(SettingsProcess.PrepareSetupProcess, false)
                        }
                    }
                } else {
                    repository.setCurrentProcess(SettingsProcess.PrepareSetupProcess, false)
                }
            }.addOnFailureListener {
                repository.setCurrentSwitch(SettingsSwitch.DrawSwitch, false)
                repository.setCurrentProcess(SettingsProcess.PrepareSetupProcess, false)
            }
        }
    }

    fun checkPermissionForFloatingBubble() {
        if (repository.getCurrentSwitch(SettingsSwitch.FloatingBubbleSwitch) &&
            !repository.getCurrentProcessSnapshot(SettingsProcess.PrepareSetupProcess)
        ) {
            repository.setCurrentProcess(SettingsProcess.PrepareSetupProcess, true)
            if (Build.VERSION.SDK_INT >= 33) {
                checkPostNotificationForFloatingBubble()
            } else checkDrawOverlayForFloatingBubble()
        } else {
            repository.setCurrentProcess(SettingsProcess.PrepareSetupProcess, false)
            NotificationFloatingBubbleService.stopService(activity)
        }
    }

    private val postNotificationPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        repository.setCurrentSwitch(SettingsSwitch.FloatingBubbleSwitch, false)
        repository.setCurrentProcess(SettingsProcess.PrepareSetupProcess, false)
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
                    repository.setCurrentSwitch(SettingsSwitch.FloatingBubbleSwitch, false)
                    repository.setCurrentProcess(SettingsProcess.PrepareSetupProcess, false)
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
                    repository.setCurrentSwitch(SettingsSwitch.FloatingBubbleSwitch, false)
                    repository.setCurrentProcess(SettingsProcess.PrepareSetupProcess, false)
                }
                repository.setCurrentSwitch(SettingsSwitch.FloatingBubbleSwitch, false)
                repository.setCurrentProcess(SettingsProcess.PrepareSetupProcess, false)
            }
        } else {
            repository.setCurrentSwitch(SettingsSwitch.FloatingBubbleSwitch, true)
            repository.setCurrentProcess(SettingsProcess.PrepareSetupProcess, false)
            NotificationFloatingBubbleService.startService(activity)
        }
    }

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