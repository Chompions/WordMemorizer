package com.sawelo.wordmemorizer.util

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

    const val NOTIFICATION_START_ACTION = "NOTIFICATION_START_ACTION"
    const val NOTIFICATION_STOP_ACTION = "NOTIFICATION_STOP_ACTION"

    val RECEIVER_OPEN_ACTION = "$PACKAGE_NAME.action.OPEN_FLOATING_DIALOG"
    val RECEIVER_CLOSE_ACTION = "$PACKAGE_NAME.action.CLOSE_FLOATING_DIALOG"

    const val SORTING_ORDER = "SORTING_ORDER"
    const val SORTING_ANCHOR = "SORTING_ANCHOR"

    var isDrawWindowFadeVisibility = false
    var isAddWordWindowActive = false
}