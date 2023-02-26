package com.sawelo.wordmemorizer.util

import com.sawelo.wordmemorizer.BuildConfig
import com.sawelo.wordmemorizer.data.data_class.entity.Category
import kotlinx.coroutines.flow.MutableStateFlow

object Constants {

    // Fragment tags and args
    const val HOME_FRAGMENT_TAG = "HOME_FRAGMENT_TAG"
    const val WORD_LIST_FRAGMENT_TAG = "WORD_LIST_FRAGMENT_TAG"
    const val SETTINGS_FRAGMENT_TAG = "SETTINGS_FRAGMENT_TAG"

    // Notifications
    const val NOTIFICATION_START_ACTION = "NOTIFICATION_START_ACTION"
    const val NOTIFICATION_STOP_ACTION = "NOTIFICATION_STOP_ACTION"
    const val NOTIFICATION_HIDE_ACTION = "NOTIFICATION_HIDE_ACTION"
    const val NOTIFICATION_REVEAL_ACTION = "NOTIFICATION_REVEAL_ACTION"
    const val NOTIFICATION_WRAP_ACTION = "NOTIFICATION_WRAP_ACTION"
    const val NOTIFICATION_UNWRAP_ACTION = "NOTIFICATION_UNWRAP_ACTION"

    const val NOTIFICATION_TRANSLATOR_ID = 21
    const val NOTIFICATION_DRAW_DIGITAL_INK_ID = 22

    // Broadcast receiver
    const val RECEIVER_OPEN_ACTION = "${BuildConfig.APPLICATION_ID}.action.OPEN_FLOATING_DIALOG"
    const val RECEIVER_CLOSE_ACTION = "${BuildConfig.APPLICATION_ID}.action.CLOSE_FLOATING_DIALOG"

    const val RECEIVER_OPEN_FLOATING_DIALOG_REQUEST_CODE = 1
    const val SERVICE_CLOSE_FLOATING_BUBBLE_REQUEST_CODE = 2
    const val SERVICE_WRAP_FLOATING_BUBBLE_REQUEST_CODE = 3

    var floatingBubbleIsWrappedOnNotif = false
    val selectedCategories = MutableStateFlow<List<Category>>(emptyList())
}