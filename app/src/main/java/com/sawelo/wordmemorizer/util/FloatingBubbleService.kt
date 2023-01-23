package com.sawelo.wordmemorizer.util

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.util.NotificationUtils.NOTIFICATION_START_ACTION
import com.sawelo.wordmemorizer.util.NotificationUtils.NOTIFICATION_STOP_ACTION

class FloatingBubbleService : Service() {

    private var floatingDialogReceiver: FloatingDialogReceiver? = null
    private var floatingBubbleView: View? = null
    private var windowManager: WindowManager? = null

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
        if (floatingDialogReceiver != null) {
            val receiverIntent = Intent()
            receiverIntent.action = NOTIFICATION_STOP_ACTION
            sendBroadcast(receiverIntent)

            unregisterReceiver(floatingDialogReceiver)
            floatingDialogReceiver = null
        }

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

        floatingDialogReceiver = FloatingDialogReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(NOTIFICATION_START_ACTION)
        intentFilter.addAction(NOTIFICATION_STOP_ACTION)
        registerReceiver(floatingDialogReceiver, intentFilter)

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
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.x = resources.displayMetrics.widthPixels / 2

        var mInitialX = 0
        var mInitialY = 0
        var mInitialTouchX: Float = 0.toFloat()
        var mInitialTouchY: Float = 0.toFloat()

        val maxClickDuration = 200L
        var startClickDuration = 0L

        if (floatingBubbleView != null) {
            floatingBubbleView!!.setOnTouchListener { _, event ->
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startClickDuration = System.currentTimeMillis()

                        mInitialX = params.x
                        mInitialY = params.y
                        mInitialTouchX = event.rawX
                        mInitialTouchY = event.rawY
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = (mInitialX + (event.rawX - mInitialTouchX)).toInt()
                        params.y = (mInitialY + (event.rawY - mInitialTouchY)).toInt()
                        windowManager?.updateViewLayout(floatingBubbleView, params)
                    }
                    MotionEvent.ACTION_UP -> {
                        val endClickDuration = System.currentTimeMillis() - startClickDuration
                        if (endClickDuration < maxClickDuration) {
                            floatingBubbleView!!.performClick()
                            val receiverIntent = Intent()
                            receiverIntent.action = NOTIFICATION_START_ACTION
                            sendBroadcast(receiverIntent)
                        }
                    }
                }
                false
            }
        }
        windowManager?.addView(floatingBubbleView, params)
    }

    companion object {
        private const val CHANNEL_ID = "FLOATING_BUBBLE_CHANNEL_ID"
        private const val NOTIFICATION_ID = 1
    }
}