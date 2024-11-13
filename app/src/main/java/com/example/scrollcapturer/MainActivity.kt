package com.example.scrollcapturer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.scrollcapturer.ui.theme.ScrollCapturerTheme
import androidx.navigation.compose.rememberNavController
import com.example.scrollcapturer.resultScreen.ResultScreen
import com.example.scrollcapturer.screenshotListScreen.ScreenshotListScreen
import com.example.scrollcapturer.screenshotListScreen.ScreenshotListSharedViewModel
import com.example.scrollcapturer.stitchscreen.StitchScreen
import com.example.scrollcapturer.stitchscreen.StitchScreenViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.opencv.android.OpenCVLoader

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.POST_NOTIFICATIONS
            ),
            0
        )

        // Foreground Service
        val channel = NotificationChannel(
            "AUTO_SCROLL_CAPTURE_CHANNEL",
            "AutoScrollCapture Notifications",
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        // OpenCV for feature matching
        if (OpenCVLoader.initLocal()) {
            Log.i("OpenCV", "OpenCV successfully loaded.")
        } else {
            Log.e("OpenCV", "OpenCV initialization failed.")
        }.toInt()

        enableEdgeToEdge()
        setContent {
            ScrollCapturerTheme {
                Column(
                    modifier = Modifier
                        .background(color = Color.White)
                        .padding(WindowInsets.statusBars.asPaddingValues())
                        .padding(WindowInsets.navigationBars.asPaddingValues())
                )
                {
                    val navController = rememberNavController()
                    val sharedViewModel: ScreenshotListSharedViewModel = hiltViewModel()
                    val stitchScreenViewModel: StitchScreenViewModel = hiltViewModel()

                    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
                    val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues()
                    val density = LocalDensity.current
                    val statusBarHeightPx = with(density) { statusBarPadding.calculateTopPadding().toPx().toInt() }
                    val navigationBarHeightPx = with(density) { navigationBarPadding.calculateBottomPadding().toPx().toInt() }
                    LaunchedEffect(statusBarHeightPx, navigationBarHeightPx) {
                        stitchScreenViewModel.setInsets(statusBarHeightPx, navigationBarHeightPx)
                    }

                    NavHost(
                        navController = navController,
                        startDestination = "screenshot_list_screen"
                    ) {
                        composable("screenshot_list_screen") {
                            ScreenshotListScreen(navController, sharedViewModel)
                        }
                        composable("stitch_screen") {
                            StitchScreen(navController, sharedViewModel, stitchScreenViewModel)
                        }
                        composable("result_screen") {
                            ResultScreen(navController, stitchScreenViewModel)
                        }
                    }
                }
            }
        }
    }
}
