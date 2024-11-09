package com.example.scrollcapturer.services

import android.app.Activity
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.ImageFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.scrollcapturer.R


class ScreenCaptureService : Service() {

    private val tag = "ScreenCaptureService"
    private var imageReader: ImageReader? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaProjection: MediaProjection? = null

    private var isScrolling = false
    private val handler = android.os.Handler(Looper.getMainLooper())

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(tag, intent.toString())
        when (intent?.action) {
            Actions.START_PROJECTION.toString() -> startProjection(intent)
            Actions.STOP_PROJECTION.toString() -> stopProjection()
            Actions.START_AUTO_CAPTURE.toString() -> startAutoScrollAndCapture()
            Actions.CAPTURE_SCREEN.toString() -> captureCurrentScreen()
            Actions.STOP_CONTINUOUS_SCROLL.toString() -> isScrolling = false
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()

        // create temporary directory for storing captured screenshots

    }

    private fun startAutoScrollAndCapture() {
        Log.d(tag, "startAutoScrollAndCapture()")

        // intent for accessibility service to collapse status bar
        val collapseStatusBarIntent = Intent(this, GestureScrollService::class.java)
        collapseStatusBarIntent.action = GestureScrollService.Actions.COLLAPSE_STATUS_BAR.toString()
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
        scrollDownByHalfPageIntent.action =
            GestureScrollService.Actions.SCROLL_DOWN_HALF_PAGE.toString()
        startService(scrollDownByHalfPageIntent)

        handler.postDelayed({
            continuousScrollDownPage()
        }, 1750)
    }

    private fun startProjection(intent: Intent) {
        Log.d(tag, "startProjection()")

        val notification = makeServiceNotification()
        startForeground(2, notification)

        val metrics = resources.displayMetrics

        // initialize imageReader
        imageReader = ImageReader.newInstance(
            metrics.widthPixels,
            metrics.heightPixels,
            ImageFormat.RGB_565,
            2
        )

        val resultCode = intent.getIntExtra("resultCode", Activity.RESULT_CANCELED)
        val data = intent.getParcelableExtra("data", Intent::class.java)
        if (data == null) {
            Log.e(tag, "Invalid data for MediaProjection")
            stopSelf()
            return
        }

        val mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)

        // register callback for onStop() before starting capture session
        mediaProjection?.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                super.onStop()
                stopProjection()
            }
        }, null)

        // create virtual display
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            metrics.widthPixels,
            metrics.heightPixels,
            metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader!!.surface,
            null, null
        )
    }

    private fun stopProjection() {
        Log.d(tag, "stopProjection()")
        virtualDisplay?.release()
        imageReader?.close()

        virtualDisplay = null
        imageReader = null
        mediaProjection = null
    }

    private fun captureCurrentScreen() {
        Log.d(tag, "captureScreen()")

    }

    private fun makeServiceNotification(): Notification {

        // Start button
        val startAutoCaptureIntent = Intent(this, ScreenCaptureService::class.java)
        startAutoCaptureIntent.action = Actions.START_AUTO_CAPTURE.toString()
        val startPendingIntent = PendingIntent.getService(
            this,
            0,
            startAutoCaptureIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Stop button
        val stopProjectionIntent = Intent(this, ScreenCaptureService::class.java)
        stopProjectionIntent.action = Actions.STOP_PROJECTION.toString()
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopProjectionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "AUTO_SCROLL_CAPTURE_CHANNEL")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Screen Capture Running")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(R.drawable.ic_launcher_foreground, "Start", startPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Stop", stopPendingIntent)
            .build()

        return notification
    }

    enum class Actions {
        START_PROJECTION, STOP_PROJECTION, CAPTURE_SCREEN, START_AUTO_CAPTURE, STOP_CONTINUOUS_SCROLL
    }
}