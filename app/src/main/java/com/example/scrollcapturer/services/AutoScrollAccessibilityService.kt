package com.example.scrollcapturer.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.content.Intent
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class AutoScrollAccessibilityService : AccessibilityService() {

    private var screenHeight: Int = 0
    private var screenWidth: Int = 0
    private var isScrolling = false
    private val handler = android.os.Handler(Looper.getMainLooper())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d("AS_Service", "onStartCommand called with isScrolling = $isScrolling")

        if (intent != null && intent.action == "com.example.scrollcapturer.START_AUTO_SCROLL") {

            collapseStatusBar()

            // wait 1 second
            handler.postDelayed({
                isScrolling = true
                performAutoScroll()
            }, 1000)
        }

        return START_STICKY
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("AS_AS", "Accessibility Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        if (event == null) return

        when (event.eventType) {

            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {}

            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                Log.d("AS_AS", "View scrolled $event")
            }

            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                stopAutoScroll()
            }

            else -> Log.d(
                "AS_AS",
                "Unhandled event: ${event.eventType}, ${event.action}, ${event.text}, $event."
            )
        }
    }

    override fun onCreate() {
        super.onCreate()
        val displayMetrics = resources.displayMetrics
        screenHeight = displayMetrics.heightPixels
        screenWidth = displayMetrics.widthPixels

        Log.d("AS_AS", "screenHeight: $screenHeight, screenWidth: $screenWidth")
    }

    override fun onInterrupt() {

    }

    private fun performAutoScroll() {
        if (!isScrolling) return

        scrollDownByHalfPage()

        handler.postDelayed({
            performAutoScroll()
        }, 1750)
    }

    private fun stopAutoScroll() {
        isScrolling = false
    }

    // scroll down by half the page (half of screen height)
    private fun scrollDownByHalfPage() {
        val path = Path().apply {
            // move from 3/4 of screen to 1/4 of screen
            moveTo(screenWidth / 2f, screenHeight * 3 / 4f)
            lineTo(screenWidth / 2f, screenHeight * 1 / 4f)
        }

        val strokeDescription = GestureDescription.StrokeDescription(path, 0L, 800L)
        val gestureDescription = GestureDescription.Builder().addStroke(strokeDescription).build()
        dispatchGesture(gestureDescription, null, null)

        Log.d("AS_AS", "scrollDownByHalfPage()")
    }

    // assumes the status bar is opened,
    // create a gesture that simulates a swipe from bottom to top
    private fun collapseStatusBar() {
        val path = Path().apply {
            // swipe from bottom of the screen to top of the screen
            moveTo(screenWidth / 2f, screenHeight.toFloat() - 50)
            lineTo(screenWidth / 2f, 0f)
        }

        val strokeDescription = GestureDescription.StrokeDescription(path, 0L, 500L)
        val gestureDescription = GestureDescription.Builder().addStroke(strokeDescription).build()
        dispatchGesture(gestureDescription, null, null)

        Log.d("AS_AS", "collapseStatusBar()")
    }

}