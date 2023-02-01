package com.sawelo.wordmemorizer.util

import com.sawelo.wordmemorizer.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow

object Constants {
    /**
     * These variables were set in MainApplication,
     * required to be set before everything else
     */
    var PACKAGE_NAME = ""
    var PREFERENCE_FLOATING_BUBBLE_KEY = ""
    var PREFERENCE_OFFLINE_TRANSLATION_KEY = ""
    var PREFERENCE_DRAW_CHARACTER_KEY = ""

    // Fragment tags and args
    const val HOME_FRAGMENT_TAG = "HOME_FRAGMENT_TAG"
    const val WORD_LIST_FRAGMENT_CATEGORY_ARGS = "WORD_LIST_FRAGMENT_CATEGORY_ARGS"
    const val WORD_LIST_FRAGMENT_CATEGORY_LIST_ARGS = "WORD_LIST_FRAGMENT_CATEGORY_LIST_ARGS"

    // Notifications
    const val NOTIFICATION_START_ACTION = "NOTIFICATION_START_ACTION"
    const val NOTIFICATION_STOP_ACTION = "NOTIFICATION_STOP_ACTION"
    const val NOTIFICATION_HIDE_ACTION = "NOTIFICATION_HIDE_ACTION"
    const val NOTIFICATION_REVEAL_ACTION = "NOTIFICATION_REVEAL_ACTION"

    const val NOTIFICATION_DOWNLOAD_EXTRA = "NOTIFICATION_DOWNLOAD_EXTRA"
    const val NOTIFICATION_DOWNLOAD_TRANSLATOR = "NOTIFICATION_DOWNLOAD_TRANSLATOR"
    const val NOTIFICATION_DOWNLOAD_DRAW_DIGITAL_INK = "NOTIFICATION_DOWNLOAD_DRAW_DIGITAL_INK"

    // Broadcast receiver
    const val RECEIVER_OPEN_ACTION = "${BuildConfig.APPLICATION_ID}.action.OPEN_FLOATING_DIALOG"
    const val RECEIVER_CLOSE_ACTION = "${BuildConfig.APPLICATION_ID}.action.CLOSE_FLOATING_DIALOG"

    const val RECEIVER_OPEN_FLOATING_DIALOG_REQUEST_CODE = 1
    const val SERVICE_CLOSE_FLOATING_BUBBLE_REQUEST_CODE = 2
    const val SERVICE_HIDE_SHOW_FLOATING_BUBBLE_REQUEST_CODE = 3

    var isOfflineTranslationDownloadingFlow = MutableStateFlow(false)
    var isDrawDigitalInkDownloadingFlow = MutableStateFlow(false)
}