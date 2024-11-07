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
            Actions.START.toString() -> start()
            Actions.STOP.toString() -> stopSelf()
            Actions.START_AUTO_CAPTURE.toString() -> startAutoScrollAndCapture()
        }
        return START_STICKY
    }

    private fun start() {

        // Start Auto Capture Button
        val startAutoCaptureIntent = Intent(this, AutoScrollCaptureService::class.java)
        startAutoCaptureIntent.apply {
            action = Actions.START_AUTO_CAPTURE.toString()
        }
        val startPendingIntent = PendingIntent.getService(
            this,
            0,
            startAutoCaptureIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Stop Service Button
        val stopAutoCaptureIntent = Intent(this, AutoScrollCaptureService::class.java)
        stopAutoCaptureIntent.apply {
            action = Actions.STOP.toString()
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopAutoCaptureIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        val notification = NotificationCompat.Builder(this, "AUTO_SCROLL_CAPTURE_CHANNEL")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Auto Scroll & Capture")
            .setOngoing(true)   // make the notification non-dismissible
            .addAction(R.drawable.ic_launcher_foreground, "Start", startPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Stop", stopPendingIntent)
            .build()
        startForeground(1, notification)
    }

    private fun startAutoScrollAndCapture() {

    }

    enum class Actions {
        START, START_AUTO_CAPTURE, STOP
    }

}