package com.example.tapjacktester

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat

class OverlayService : Service() {

    private lateinit var wm: WindowManager
    private var overlayView: View? = null
    private var currentMode = MODE_PASSTHROUGH

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        startForeground(NOTIF_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val mode = intent?.getStringExtra(EXTRA_MODE) ?: currentMode

        when (action) {
            ACTION_SHOW -> showOverlay(mode)
            ACTION_HIDE -> hideOverlay()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        removeOverlayView()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun showOverlay(mode: String) {
        if (overlayView != null) {
            removeOverlayView()
        }
        currentMode = mode

        val overlay = FrameLayout(this).apply {
            when (mode) {
                MODE_PASSTHROUGH -> {
                    // Fullscreen transparent red overlay that lets touches pass through
                    setBackgroundColor(Color.argb(128, 255, 0, 0))
                }
                MODE_INTERCEPT -> {
                    // Fullscreen red overlay that consumes touch events
                    setBackgroundColor(Color.argb(180, 255, 60, 0))
                    isClickable = true
                    isFocusable = true
                }
                MODE_CREDENTIAL_FAKE -> {
                    // Synthetic login overlay mimicking a fake dialog over underlying screen
                    setBackgroundColor(Color.argb(220, 30, 30, 30))
                }
            }
        }

        var flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS

        if (mode == MODE_PASSTHROUGH) {
            flags = flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        } else {
            // MODE_INTERCEPT or MODE_CREDENTIAL_FAKE allows touches on the overlay itself
            flags = flags or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            flags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        try {
            wm.addView(overlay, params)
            overlayView = overlay
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideOverlay() {
        removeOverlayView()
    }

    private fun removeOverlayView() {
        overlayView?.let {
            try {
                wm.removeView(it)
            } catch (_: Throwable) {}
        }
        overlayView = null
    }

    private fun buildNotification(): Notification {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Tapjack Tester",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Tapjack Tester running")
            .setContentText("Overlay service active")
            .setOngoing(true)
            .build()
    }

    companion object {
        const val ACTION_SHOW = "overlay_show"
        const val ACTION_HIDE = "overlay_hide"
        const val EXTRA_MODE = "extra_mode"

        const val MODE_PASSTHROUGH = "passthrough"
        const val MODE_INTERCEPT = "intercept"
        const val MODE_CREDENTIAL_FAKE = "credential_fake"

        private const val CHANNEL_ID = "tapjacktester"
        private const val NOTIF_ID = 1001
    }
}
