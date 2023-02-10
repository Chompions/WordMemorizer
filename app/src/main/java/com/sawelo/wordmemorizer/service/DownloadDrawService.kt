package com.sawelo.wordmemorizer.service

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier
import com.sawelo.wordmemorizer.fragment.SettingsSwitch
import com.sawelo.wordmemorizer.util.Constants
import com.sawelo.wordmemorizer.util.Constants.NOTIFICATION_DRAW_DIGITAL_INK_ID
import com.sawelo.wordmemorizer.util.ViewUtils.showToast
import com.sawelo.wordmemorizer.util.enum_class.SettingsProcess

class DownloadDrawService : BaseDownloadService(
    NOTIFICATION_DRAW_DIGITAL_INK_ID,
    "Downloading character recognizer"
) {
    override fun downloadProcess() {
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

    companion object {
        fun startService(context: Context) {
            val drawDigitalInkIntent = Intent(context, DownloadDrawService::class.java)
            drawDigitalInkIntent.action = Constants.NOTIFICATION_START_ACTION
            context.startForegroundService(drawDigitalInkIntent)
        }
    }
}