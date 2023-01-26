package com.sawelo.wordmemorizer.view

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.constraintlayout.widget.ConstraintLayout
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.util.callback.BackButtonListener

class DialogWindowConstraintLayout @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null
): ConstraintLayout(context, attributeSet) {
    private var backButtonListener: BackButtonListener? = null

    fun setBackButtonListener(listener: BackButtonListener) {
        backButtonListener = listener
    }

    init {
        setBackgroundResource(R.drawable.rounded_background)
        elevation = 6F
        id = R.id.dialogWindow_constraintLayout
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event?.keyCode == KeyEvent.KEYCODE_BACK) {
            backButtonListener?.onBackButtonListener()
            return true
        }
        return super.dispatchKeyEvent(event)
    }
}