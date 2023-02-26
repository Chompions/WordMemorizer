package com.sawelo.wordmemorizer.service.download

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.sawelo.wordmemorizer.data.preferences.settings.SettingsProcess
import com.sawelo.wordmemorizer.data.preferences.settings.SettingsSwitch
import com.sawelo.wordmemorizer.ui.ui_util.ViewUtils.showToast
import com.sawelo.wordmemorizer.util.Constants
import com.sawelo.wordmemorizer.util.Constants.NOTIFICATION_TRANSLATOR_ID

class DownloadTranslatorService : BaseDownloadService(
    NOTIFICATION_TRANSLATOR_ID,
    "Downloading offline Japanese translator"
) {
    override fun downloadProcess() {
        showToast("Downloading offline Japanese translator, please wait")
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

    companion object {
        fun startService(context: Context) {
            val translatorIntent = Intent(context, DownloadTranslatorService::class.java)
            translatorIntent.action = Constants.NOTIFICATION_START_ACTION
            context.startForegroundService(translatorIntent)
        }
    }
}