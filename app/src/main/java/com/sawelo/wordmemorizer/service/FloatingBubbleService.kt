package com.sawelo.wordmemorizer.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.receiver.FloatingAddWordWindowReceiver
import com.sawelo.wordmemorizer.receiver.FloatingAddWordWindowReceiver.Companion.registerReceiver
import com.sawelo.wordmemorizer.receiver.FloatingAddWordWindowReceiver.Companion.unregisterReceiver
import com.sawelo.wordmemorizer.util.Constants.CLOSE_FLOATING_SERVICE_REQUEST_CODE
import com.sawelo.wordmemorizer.util.Constants.NOTIFICATION_HIDE_ACTION
import com.sawelo.wordmemorizer.util.Constants.NOTIFICATION_REVEAL_ACTION
import com.sawelo.wordmemorizer.util.Constants.NOTIFICATION_START_ACTION
import com.sawelo.wordmemorizer.util.Constants.NOTIFICATION_STOP_ACTION
import com.sawelo.wordmemorizer.util.Constants.PREFERENCE_FLOATING_BUBBLE_KEY

class FloatingBubbleService : Service(), OnTouchListener {

    private var floatingBubbleView: View? = null

    private var windowManager: WindowManager? = null
    private var params: WindowManager.LayoutParams? = null

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
        floatingAddWordWindowReceiver = FloatingAddWordWindowReceiver()
        floatingAddWordWindowReceiver?.registerReceiver(this)

        when (intent.action) {
            NOTIFICATION_STOP_ACTION -> {
                FloatingAddWordWindowReceiver.closeWindow(this)
                floatingAddWordWindowReceiver?.unregisterReceiver(this)
                floatingAddWordWindowReceiver = null
                stopSelf()
            }
            NOTIFICATION_START_ACTION -> createNotification()
            NOTIFICATION_HIDE_ACTION -> hideFloatingBubble()
            NOTIFICATION_REVEAL_ACTION -> revealFloatingBubble()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        if (floatingBubbleView != null) {
            windowManager?.removeView(floatingBubbleView)
            floatingBubbleView = null
        }
        windowManager = null

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.edit {
            putBoolean(PREFERENCE_FLOATING_BUBBLE_KEY, false)
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

        val stopServiceIntent = Intent(this, FloatingBubbleService::class.java)
        stopServiceIntent.action = NOTIFICATION_STOP_ACTION
        val stopServicePendingIntent = PendingIntent.getService(
            this,
            CLOSE_FLOATING_SERVICE_REQUEST_CODE,
            stopServiceIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val openDialogPendingIntent =
            FloatingAddWordWindowReceiver.openWindowPendingIntent(this, null)

        // Create notification builder
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Floating bubble")
            .setContentText("Tap here to open the add dialog")
            .setShowWhen(false)
            .setContentIntent(openDialogPendingIntent)
            .addAction(
                R.drawable.ic_baseline_close_24,
                "Close the floating bubble",
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
        params?.x = resources.displayMetrics.widthPixels / 2 - 100

        if (floatingBubbleView != null) {
            floatingBubbleView!!.setOnTouchListener(this)
        }
        windowManager?.addView(floatingBubbleView, params)
    }

    private fun hideFloatingBubble() {
        floatingBubbleView?.isVisible = false
    }

    private fun revealFloatingBubble() {
        floatingBubbleView?.isVisible = true
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels / 2
        val screenHeight = Resources.getSystem().displayMetrics.heightPixels / 2
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
                    .coerceIn(-screenWidth + 100, screenWidth - 100)
                params?.y = (mInitialY + (event.rawY - mInitialTouchY)).toInt()
                    .coerceIn(-screenHeight + 100, screenHeight - 100)
                windowManager?.updateViewLayout(floatingBubbleView, params)
            }
            MotionEvent.ACTION_UP -> {
                val endClickDuration = System.currentTimeMillis() - startClickDuration
                if (endClickDuration < maxClickDuration) {
                    floatingBubbleView!!.performClick()
                    FloatingAddWordWindowReceiver.openWindow(this, null)
                }
            }
        }
        return false
    }

    companion object {
        private const val CHANNEL_ID = "FLOATING_BUBBLE_CHANNEL_ID"
        private const val NOTIFICATION_ID = 1

        fun startService(context: Context) {
            val serviceIntent = Intent(context, FloatingBubbleService::class.java)
            serviceIntent.action = NOTIFICATION_START_ACTION
            context.startForegroundService(serviceIntent)
        }

        fun hideBubbleService(context: Context) {
            val serviceIntent = Intent(context, FloatingBubbleService::class.java)
            serviceIntent.action = NOTIFICATION_HIDE_ACTION
            context.startService(serviceIntent)
        }

        fun revealBubbleService(context: Context) {
            val serviceIntent = Intent(context, FloatingBubbleService::class.java)
            serviceIntent.action = NOTIFICATION_REVEAL_ACTION
            context.startService(serviceIntent)
        }

        fun stopService(context: Context) {
            val serviceIntent = Intent(context, FloatingBubbleService::class.java)
            serviceIntent.action = NOTIFICATION_STOP_ACTION
            context.stopService(serviceIntent)
        }
    }
}