package com.example.scrollcapturer.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class GestureScrollService : AccessibilityService() {

    private var screenHeight: Int = 0
    private var screenWidth: Int = 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when (intent?.action) {
            Actions.COLLAPSE_STATUS_BAR.toString() -> collapseStatusBar()
            Actions.SCROLL_DOWN_HALF_PAGE.toString() -> scrollDownByHalfPage()
        }

        return START_STICKY
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        Log.d("AS_onServiceConnected()", "Accessibility Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        if (event == null) return

        when (event.eventType) {

            AccessibilityEvent.TYPE_VIEW_CLICKED -> stopAutoScroll()

            else -> {}
//                Log.d(                "AS_AS",
//                "Unhandled event: ${event.eventType}, ${event.action}, ${event.text}, $event."
//            )
        }
    }

    // called when user enable use accessibility service in Accessibility Settings
    override fun onCreate() {
        super.onCreate()
        val displayMetrics = resources.displayMetrics
        screenHeight = displayMetrics.heightPixels
        screenWidth = displayMetrics.widthPixels

        Log.d("AS_onCreate()", "screenHeight: $screenHeight, screenWidth: $screenWidth")
    }

    override fun onInterrupt() {

    }

    private fun stopAutoScroll() {

        // intent for AutoScrollCaptureService to stop continuous-scrolling
        val stopContinuousScroll = Intent(this, ScreenCaptureService::class.java)
        stopContinuousScroll.action = ScreenCaptureService.Actions.STOP_CONTINUOUS_SCROLL.toString()
        startService(stopContinuousScroll)

        Log.d("AS", "stopAutoScroll()")
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

        Log.d("AS", "scrollDownByHalfPage()")
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

        Log.d("AS", "collapseStatusBar()")
    }

    enum class Actions {
        COLLAPSE_STATUS_BAR, SCROLL_DOWN_HALF_PAGE
    }
}