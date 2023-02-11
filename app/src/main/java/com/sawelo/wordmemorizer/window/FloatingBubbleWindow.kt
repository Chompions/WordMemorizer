package com.sawelo.wordmemorizer.window

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.view.*
import android.view.WindowManager.LayoutParams
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.receiver.FloatingAddWordWindowReceiver

class FloatingBubbleWindow(
    private val context: Context
    ) : BaseWindow(context), View.OnTouchListener {

    private var mInitialX = 0
    private var mInitialY = 0
    private var mInitialTouchX: Float = 0.toFloat()
    private var mInitialTouchY: Float = 0.toFloat()

    private val maxClickDuration = 200L
    private var startClickDuration = 0L

    private var mView: ViewGroup? = null
    private var mParams: LayoutParams? = null

    @SuppressLint("InflateParams")
    override fun setViews(layoutInflater: LayoutInflater): ViewGroup {
        mView = layoutInflater.inflate(R.layout.window_floating_bubble, null) as ViewGroup
        mView!!.setOnTouchListener(this)
        return mView as ViewGroup
    }

    override fun setParams(params: LayoutParams): LayoutParams {
        mParams = params.apply {
            type = LayoutParams.TYPE_APPLICATION_OVERLAY
            flags = LayoutParams.FLAG_NOT_FOCUSABLE
            width = LayoutParams.WRAP_CONTENT
            x = context.resources.displayMetrics.widthPixels / 2 - 100
        }
        return mParams as LayoutParams
    }

    override fun beforeShowWindow() {
        isWindowActive = true
    }

    override fun beforeCloseWindow() {
        isWindowActive = false
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels / 2
        val screenHeight = Resources.getSystem().displayMetrics.heightPixels / 2
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startClickDuration = System.currentTimeMillis()

                mInitialX = mParams?.x ?: 0
                mInitialY = mParams?.y ?: 0
                mInitialTouchX = event.rawX
                mInitialTouchY = event.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                mParams?.x = (mInitialX + (event.rawX - mInitialTouchX)).toInt()
                    .coerceIn(-screenWidth + 100, screenWidth - 100)
                mParams?.y = (mInitialY + (event.rawY - mInitialTouchY)).toInt()
                    .coerceIn(-screenHeight + 100, screenHeight - 100)

                windowManager.updateViewLayout(mView, mParams)
            }
            MotionEvent.ACTION_UP -> {
                val endClickDuration = System.currentTimeMillis() - startClickDuration
                if (endClickDuration < maxClickDuration) {
                    mView!!.performClick()
                    FloatingAddWordWindowReceiver.openWindow(context)
                }
            }
        }
        return false
    }

    companion object {
        var isWindowActive = false
            private set
    }
}