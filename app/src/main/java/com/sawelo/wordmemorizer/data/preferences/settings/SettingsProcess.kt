package com.sawelo.wordmemorizer.data.preferences.settings

import com.sawelo.wordmemorizer.data.preferences.base.BaseProcess

enum class SettingsProcess : BaseProcess {
    PrepareSetupProcess {
        override fun processKey(): String = "PROCESS_PREPARING_DOWNLOAD_KEY"
    },
    TranslationDownload {
        override fun processKey(): String = "PROCESS_OFFLINE_TRANSLATION_DOWNLOADING"
    },
    DrawDownload {
        override fun processKey(): String = "PROCESS_DRAW_CHARACTER_DOWNLOADING"
    }
}