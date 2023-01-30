package com.sawelo.wordmemorizer.util

import kotlinx.coroutines.flow.MutableStateFlow

object Constants {
    /**
     * These variables were set in MainApplication, required to be set before everything else
     */
    var PACKAGE_NAME = ""
    var PREFERENCE_FLOATING_BUBBLE_KEY = ""
    var PREFERENCE_OFFLINE_TRANSLATION_KEY = ""
    var PREFERENCE_DRAW_CHARACTER_KEY = ""

    const val HOME_FRAGMENT_TAG = "HOME_FRAGMENT_TAG"

    const val WORD_LIST_FRAGMENT_CATEGORY_ARGS = "WORD_LIST_FRAGMENT_CATEGORY_ARGS"
    const val WORD_LIST_FRAGMENT_CATEGORY_LIST_ARGS = "WORD_LIST_FRAGMENT_CATEGORY_LIST_ARGS"

    // Notifications
    const val NOTIFICATION_START_ACTION = "NOTIFICATION_START_ACTION"
    const val NOTIFICATION_STOP_ACTION = "NOTIFICATION_STOP_ACTION"

    const val NOTIFICATION_DOWNLOAD_EXTRA = "NOTIFICATION_DOWNLOAD_EXTRA"
    const val NOTIFICATION_DOWNLOAD_TRANSLATOR = "NOTIFICATION_DOWNLOAD_TRANSLATOR"
    const val NOTIFICATION_DOWNLOAD_DRAW_DIGITAL_INK = "NOTIFICATION_DOWNLOAD_DRAW_DIGITAL_INK"

    val RECEIVER_OPEN_ACTION = "$PACKAGE_NAME.action.OPEN_FLOATING_DIALOG"
    val RECEIVER_CLOSE_ACTION = "$PACKAGE_NAME.action.CLOSE_FLOATING_DIALOG"

    const val OPEN_FLOATING_RECEIVER_REQUEST_CODE = 1
    const val CLOSE_FLOATING_SERVICE_REQUEST_CODE = 2

    const val SORTING_ORDER = "SORTING_ORDER"
    const val SORTING_ANCHOR = "SORTING_ANCHOR"

    var isDrawWindowFadeVisibility = false
    var isAddWordWindowActive = false

    var isOfflineTranslationDownloadingFlow = MutableStateFlow(false)
    var isDrawDigitalInkDownloadingFlow = MutableStateFlow(false)
}