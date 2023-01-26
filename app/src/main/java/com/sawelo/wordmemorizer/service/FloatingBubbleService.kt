package com.sawelo.wordmemorizer.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.receiver.FloatingAddWordWindowReceiver
import com.sawelo.wordmemorizer.receiver.FloatingAddWordWindowReceiver.Companion.registerReceiver
import com.sawelo.wordmemorizer.receiver.FloatingAddWordWindowReceiver.Companion.unregisterReceiver
import com.sawelo.wordmemorizer.util.NotificationUtils
import com.sawelo.wordmemorizer.util.NotificationUtils.NOTIFICATION_START_ACTION
import com.sawelo.wordmemorizer.util.NotificationUtils.NOTIFICATION_STOP_ACTION

class FloatingBubbleService : Service(), OnTouchListener {

    private var floatingBubbleView: View? = null
    private var windowManager: WindowManager? = null
    private var params:  WindowManager.LayoutParams? = null

    private var mInitialX = 0
    private var mInitialY = 0
    private var mInitialTouchX: Float = 0.toFloat()
    private var mInitialTouchY: Float = 0.toFloat()

    private val maxClickDuration = 200L
    private var startClickDuration = 0L

    private var floatingAddWordWindowReceiver: FloatingAddWordWindowReceiver? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when (intent.action) {
            NOTIFICATION_STOP_ACTION -> stopSelf()
            NOTIFICATION_START_ACTION -> createNotification()
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        floatingAddWordWindowReceiver?.unregisterReceiver(this)
        FloatingAddWordWindowReceiver.closeWindow(this)

        if (floatingBubbleView != null) {
            windowManager?.removeView(floatingBubbleView)
            floatingBubbleView = null
        }
        windowManager = null

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.edit {
            putBoolean(NotificationUtils.PREFERENCE_FLOATING_BUBBLE_KEY, false)
        }

        super.onDestroy()
    }

    private fun createNotification() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel
        val name = "Floating Bubble Channel"
        val descriptionText = "Channel for adjusting floating bubble"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        notificationManager.createNotificationChannel(channel)

        floatingAddWordWindowReceiver = FloatingAddWordWindowReceiver()
        floatingAddWordWindowReceiver?.registerReceiver(this)

        val stopServiceIntent = Intent(this, FloatingBubbleService::class.java)
        stopServiceIntent.action = NOTIFICATION_STOP_ACTION
        val stopServicePendingIntent = PendingIntent.getService(
            this, 0, stopServiceIntent, PendingIntent.FLAG_IMMUTABLE
        )

        // Create notification builder
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Floating bubble")
            .setContentText("Press the bubble to add word")
            .addAction(
                R.drawable.ic_baseline_close_24,
                "Stop floating bubble",
                stopServicePendingIntent
            )

        startForeground(NOTIFICATION_ID, builder.build())
        addFloatingBubble()
    }

    @SuppressLint("InflateParams")
    private fun addFloatingBubble() {
        val layoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatingBubbleView =
            layoutInflater.inflate(R.layout.window_floating_bubble, null)

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params?.x = resources.displayMetrics.widthPixels / 2

        if (floatingBubbleView != null) {
            floatingBubbleView!!.setOnTouchListener(this)
        }
        windowManager?.addView(floatingBubbleView, params)
    }

    companion object {
        private const val CHANNEL_ID = "FLOATING_BUBBLE_CHANNEL_ID"
        private const val NOTIFICATION_ID = 1
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startClickDuration = System.currentTimeMillis()

                mInitialX = params?.x ?: 0
                mInitialY = params?.y ?: 0
                mInitialTouchX = event.rawX
                mInitialTouchY = event.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                params?.x = (mInitialX + (event.rawX - mInitialTouchX)).toInt()
                params?.y = (mInitialY + (event.rawY - mInitialTouchY)).toInt()
                windowManager?.updateViewLayout(floatingBubbleView, params)
            }
            MotionEvent.ACTION_UP -> {
                val endClickDuration = System.currentTimeMillis() - startClickDuration
                if (endClickDuration < maxClickDuration) {
                    floatingBubbleView!!.performClick()
                    FloatingAddWordWindowReceiver.openWindow(this)
                }
            }
        }
        return false
    }
}