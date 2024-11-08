package com.example.scrollcapturer.services

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.util.Log
import com.example.scrollcapturer.utils.ImageUtils

// MediaProjectionService for screen capture
class MediaProjectionService : Service() {

    private var mediaProjectionManager: MediaProjectionManager? = null
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && intent.action == "com.example.scrollcapturer.START_AUTO_SCROLL") {
            // TODO: get mediaProjectionManager
        }
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        mediaProjectionManager = getSystemService(MediaProjectionManager::class.java)

        val metrics = resources.displayMetrics
        imageReader = ImageReader.newInstance(
            metrics.widthPixels,
            metrics.heightPixels,
            ImageFormat.RGB_565,
            2
        )

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            metrics.widthPixels,
            metrics.heightPixels,
            metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader!!.surface,
            null, null
        )

        Log.d("MS_onCreate()", "created MS")
    }

    fun captureScreen(): Bitmap? {
        val screenImage = imageReader?.acquireLatestImage()
        val screenBitmap = screenImage?.let { ImageUtils.convertImageToBitmap(it) }
        Log.d("MS_captureScreen()", "$screenBitmap")
        return screenBitmap
    }
}