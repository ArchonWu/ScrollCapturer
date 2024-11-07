package com.example.scrollcapturer.foregroundservices

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.scrollcapturer.R

class AutoScrollCaptureService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    // triggers whenever another android component sends an intent to this running services
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.START_AUTO_SCROLL.toString() -> startAutoScrollCapture()
            Actions.STOP_AUTO_SCROLL.toString() -> stopSelf()
        }
        return START_STICKY
    }

    private fun startAutoScrollCapture() {
        val notification = NotificationCompat.Builder(this, "AUTO_SCROLL_CAPTURE_CHANNEL")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Auto Scroll & Capture")
            .setContentInfo("ABCDE")
            .setOngoing(true)   // make the notification non-dismissible
            .build()
        startForeground(1, notification)
    }

    enum class Actions {
        START_AUTO_SCROLL, STOP_AUTO_SCROLL
    }

}