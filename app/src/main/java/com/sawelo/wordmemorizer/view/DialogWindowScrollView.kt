package com.sawelo.wordmemorizer.view

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.widget.ScrollView
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.util.callback.BackButtonListener

class DialogWindowScrollView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null
): ScrollView (context, attributeSet) {
    private var backButtonListener: BackButtonListener? = null

    fun setBackButtonListener(listener: BackButtonListener) {
        backButtonListener = listener
    }

    init {
        setBackgroundResource(R.drawable.rounded_background)
        elevation = 6F
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event?.keyCode == KeyEvent.KEYCODE_BACK) {
            backButtonListener?.onBackButtonListener()
            return true
        }
        return super.dispatchKeyEvent(event)
    }
}