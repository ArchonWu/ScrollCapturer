package com.example.scrollcapturer.services

import android.app.Activity
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.scrollcapturer.ImageCombiner
import com.example.scrollcapturer.MainActivity
import com.example.scrollcapturer.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class ScreenCaptureService : Service() {

    private val tag = "ScreenCaptureService"

    private var imageReader: ImageReader? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaProjection: MediaProjection? = null
    private var appDirectory: File? = null

    private var isScrolling = false
    private var isCollapsing = true

    private var screenWidth = 0
    private var screenHeight = 0

    private var imagesProduced = 0

    private val isUseStreamlined = true

    @Inject
    lateinit var imageCombiner: ImageCombiner

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(tag, intent.toString())
        when (intent?.action) {
            Actions.START_PROJECTION.name -> startProjection(intent)      // start service
            Actions.STOP_PROJECTION.name -> stopProjection()              // stop service
            Actions.START_AUTO_CAPTURE.name ->
                CoroutineScope(Dispatchers.IO).launch { executeAutoCaptureAndCombine() }    // start capture
            Actions.CAPTURE_SCREEN.name -> captureCurrentScreenToStorage()       // capture screen once
            Actions.STOP_CONTINUOUS_SCROLL.name -> if (!isCollapsing) completeCapture()      // stop auto-scrolling and capturing
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()

        // create predefined app directory for storing captured screenshots
        // path: Android/data/com.example.scrollcapturer/files/screenshots
        appDirectory = File(getExternalFilesDir(null), "/temp_screenshots")
        if (!appDirectory!!.exists()) {
            val success = appDirectory!!.mkdirs()
            if (success) {
                Log.d(tag, "onCreate(), created app directory")
            } else {
                Log.d(tag, "onCreate(), failed")
                stopSelf()
            }
        } else {
            Log.d(tag, "onCreate(), appDirectory already exists.")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag, "onDestroy")
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopProjection()
    }

    private suspend fun executeAutoCaptureAndCombine() {
        collapseStatusBar()
        delay(1000L)

        isScrolling = true
        isCollapsing = false
        imageCombiner.clearServiceCapturedImages()

        while (isScrolling) {
            val screenshotBitmap = captureScreenshot()

            if (isUseStreamlined) {
                imageCombiner.processServiceCapturedImage(screenshotBitmap)
            } else if (screenshotBitmap != null) {
                imageCombiner.addScreenshot(screenshotBitmap)
            } else {
                Log.e(tag, "screenshotBitmap is null!")
            }
            delay(1000L)

            if (isScrolling) {      // check if isScrolling has changed (completeCapture() is called)
                scrollDownHalfPage()
                delay(1500L)
            }
        }
    }

    private fun completeCapture() {
        Log.d(tag, "completeCapture()")
        isScrolling = false
        isCollapsing = true

        // intent to open the app
        val openResultIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(openResultIntent)
    }

    private fun captureScreenshot(): Bitmap? {
        Log.d(tag, "captureScreenshot()")

        var bitmap: Bitmap? = null
        val image = imageReader?.acquireLatestImage()

        try {
            image?.let {
                val planes = image.planes
                val buffer = planes[0].buffer
                val pixelStride = planes[0].pixelStride
                val rowStride = planes[0].rowStride
                val rowPadding: Int = rowStride - pixelStride * screenWidth

                // create bitmap
                bitmap = Bitmap.createBitmap(
                    screenWidth + rowPadding / pixelStride,
                    screenHeight,
                    Bitmap.Config.ARGB_8888
                )
                bitmap!!.copyPixelsFromBuffer(buffer)

            }
        } catch (e: Exception) {
            Log.e(tag, "error capturing screen")
            Log.e(tag, e.toString())
        } finally {
            image?.close()
        }

        return bitmap
    }

    private fun captureCurrentScreenToStorage() {
        Log.d(tag, "captureCurrentScreenToStorage()")

        var fos: FileOutputStream? = null
        var bitmap: Bitmap? = null
        val image = imageReader?.acquireLatestImage()

        try {
            image?.let {
                val planes = image.planes
                val buffer = planes[0].buffer
                val pixelStride = planes[0].pixelStride
                val rowStride = planes[0].rowStride
                val rowPadding: Int = rowStride - pixelStride * screenWidth

                // create bitmap
                bitmap = Bitmap.createBitmap(
                    screenWidth + rowPadding / pixelStride,
                    screenHeight,
                    Bitmap.Config.ARGB_8888
                )

                bitmap!!.copyPixelsFromBuffer(buffer)

                // write bitmap to a file
                val imageFile = File(appDirectory, "screen_$imagesProduced.png")
                fos = FileOutputStream(imageFile)
                bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, fos!!)

                imagesProduced++

                Log.e(tag, "captured image: $imagesProduced, ${imageFile.absolutePath}")
            }
        } catch (e: Exception) {
            Log.e(tag, "error capturing screen")
            Log.e(tag, e.toString())
        } finally {
            fos?.close()
            bitmap?.recycle()
            image?.close()
        }
    }

    private fun collapseStatusBar() {
        isCollapsing = true

        // intent for accessibility service to collapse status bar
        val collapseStatusBarIntent = Intent(this, GestureScrollService::class.java)
        collapseStatusBarIntent.action = GestureScrollService.Actions.COLLAPSE_STATUS_BAR.toString()
        startService(collapseStatusBarIntent)
    }

    private fun scrollDownHalfPage() {
        // intent for accessibility service to scroll down by half page
        val scrollDownByHalfPageIntent = Intent(this, GestureScrollService::class.java)
        scrollDownByHalfPageIntent.action =
            GestureScrollService.Actions.SCROLL_DOWN_HALF_PAGE.toString()
        startService(scrollDownByHalfPageIntent)
    }

    private fun startProjection(intent: Intent) {
        Log.d(tag, "startProjection()")

        val notification = makeServiceNotification()
        startForeground(2, notification)

        val metrics = resources.displayMetrics
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels

        // initialize imageReader
        imageReader = ImageReader.newInstance(
            screenWidth,
            screenHeight,
            PixelFormat.RGBA_8888,
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
                isScrolling = false
                isCollapsing = true
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

        isScrolling = false
        isCollapsing = true

        virtualDisplay?.release()
        imageReader?.close()

        virtualDisplay = null
        imageReader = null
        mediaProjection = null

        stopSelf() // stops the service
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