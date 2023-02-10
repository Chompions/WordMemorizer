package com.sawelo.wordmemorizer.util.enum_class

enum class SettingsProcess(val processKey: String) {
    PreparingDownloadProcess("PROCESS_PREPARING_DOWNLOAD_KEY"),
    FloatingBubbleSetUp("PROCESS_FLOATING_BUBBLE_SETTING_UP"),
    TranslationDownload("PROCESS_OFFLINE_TRANSLATION_DOWNLOADING"),
    DrawDownload("PROCESS_DRAW_CHARACTER_DOWNLOADING")
}