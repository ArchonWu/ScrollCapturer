package com.example.scrollcapturer.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.scrollcapturer.R

class AutoCaptureService : Service() {

    private var isScrolling = false
    private val handler = android.os.Handler(Looper.getMainLooper())

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    // triggers whenever another android component sends an intent to this running services
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.START.toString() -> start()
            Actions.STOP.toString() -> stopSelf()
            Actions.START_AUTO_CAPTURE.toString() -> startAutoScrollAndCapture()
            "com.example.scrollcapturer.STOP_CONTINUOUS_SCROLL" -> isScrolling = false
        }
        return START_STICKY
    }

    private fun start() {
        val notification = makeAutoCaptureServiceNotification()
        startForeground(1, notification)
        Log.d("ACS_start()", "hi")
    }

    private fun startAutoScrollAndCapture() {
        Log.d("S_startAutoScrollAndCapture()", "")

        // intent for accessibility service to collapse status bar
        val collapseStatusBarIntent = Intent(this, GestureScrollService::class.java)
        collapseStatusBarIntent.action = "com.example.scrollcapturer.COLLAPSE_STATUS_BAR"
        startService(collapseStatusBarIntent)

        handler.postDelayed({
            isScrolling = true
            continuousScrollDownPage()
            captureCurrentScreen()
        }, 1750)
    }

    private fun continuousScrollDownPage() {
        if (!isScrolling) return

        // intent for accessibility service to scroll down by half page
        val scrollDownByHalfPageIntent = Intent(this, GestureScrollService::class.java)
        scrollDownByHalfPageIntent.action = "com.example.scrollcapturer.SCROLL_DOWN_HALF_PAGE"
        startService(scrollDownByHalfPageIntent)

        handler.postDelayed({
            continuousScrollDownPage()
        }, 1750)
    }

    private fun captureCurrentScreen() {
        val captureScreenIntent = Intent(this, ScreenCaptureService::class.java).apply {
            action = ScreenCaptureService.Actions.CAPTURE_SCREEN.toString()
        }
        startService(captureScreenIntent)
    }

    private fun makeAutoCaptureServiceNotification(): Notification {
        // Start Auto Capture Button
        val startAutoCaptureIntent = Intent(this, AutoCaptureService::class.java)
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
        val stopAutoCaptureIntent = Intent(this, AutoCaptureService::class.java)
        stopAutoCaptureIntent.apply {
            action = Actions.STOP.toString()
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopAutoCaptureIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Test Button that doesn't do anything
        val testIntent = Intent(this, AutoCaptureService::class.java)
        testIntent.apply {
            action = Actions.TEST.toString()
        }
        val testPendingIntent = PendingIntent.getService(
            this,
            1,
            testIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "AUTO_SCROLL_CAPTURE_CHANNEL")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Auto Scroll & Capture")
            .setOngoing(true)   // make the notification non-dismissible
            .addAction(R.drawable.ic_launcher_foreground, "Start", startPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Stop", stopPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Test", testPendingIntent)
            .build()

        return notification
    }

    enum class Actions {
        START, START_AUTO_CAPTURE, STOP, TEST
    }

}