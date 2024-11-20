package com.example.scrollcapturer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.scrollcapturer.previewscreen.PreviewScreen
import com.example.scrollcapturer.previewscreen.PreviewScreenViewModel
import com.example.scrollcapturer.resultScreen.ResultScreen
import com.example.scrollcapturer.screenshotListScreen.ScreenshotListScreen
import com.example.scrollcapturer.screenshotListScreen.ScreenshotListSharedViewModel
import com.example.scrollcapturer.services.ScreenCaptureService
import com.example.scrollcapturer.ui.theme.ScrollCapturerTheme
import dagger.hilt.android.AndroidEntryPoint
import org.opencv.android.OpenCVLoader
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var screenCaptureService: ScreenCaptureService

    @Inject
    lateinit var imageCombiner: ImageCombiner

    private lateinit var navController: NavHostController
    private lateinit var previewScreenViewModel: PreviewScreenViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissions(this)
        initializeNotificationChannel(this)
        initializeOpenCV()

        enableEdgeToEdge()
        setContent {
            ScrollCapturerTheme {
                navController = rememberNavController()
                previewScreenViewModel = hiltViewModel()

                Surface {
                    Column(
                        modifier = Modifier
                            .padding(WindowInsets.statusBars.asPaddingValues())
                            .padding(WindowInsets.navigationBars.asPaddingValues())
                    )
                    {
                        val sharedViewModel: ScreenshotListSharedViewModel = hiltViewModel()

                        InitializeImageCombinerWindowInsets(imageCombiner)

                        NavHost(
                            navController = navController,
                            startDestination = "screenshot_list_screen"
                        ) {
                            composable("screenshot_list_screen") {
                                ScreenshotListScreen(navController, sharedViewModel)
                            }
                            composable("stitch_screen") {
                                PreviewScreen(
                                    navController,
                                    sharedViewModel,
                                    previewScreenViewModel
                                )
                            }
                            composable("result_screen") {
                                ResultScreen(
                                    navController,
                                    imageCombiner = imageCombiner
                                )
                            }
                        }
                    }
                }
            }
        }
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        navController.navigate("result_screen")
        Log.d("MainActivity", "navigated to result_screen")
    }
}

fun requestPermissions(activity: MainActivity) {
    ActivityCompat.requestPermissions(
        activity,
        arrayOf(
            android.Manifest.permission.POST_NOTIFICATIONS
        ),
        0
    )
}

fun initializeNotificationChannel(context: Context) {
    // Foreground Service
    val channel = NotificationChannel(
        "AUTO_SCROLL_CAPTURE_CHANNEL",
        "AutoScrollCapture Notifications",
        NotificationManager.IMPORTANCE_HIGH
    )
    val notificationManager =
        getSystemService(context, NotificationManager::class.java)
    notificationManager?.createNotificationChannel(channel)
}

fun initializeOpenCV() {
    // OpenCV for feature matching
    if (OpenCVLoader.initLocal()) {
        Log.d("OpenCV", "OpenCV successfully loaded.")
    } else {
        Log.d("OpenCV", "OpenCV initialization failed.")
    }.toInt()
}

@Composable
fun InitializeImageCombinerWindowInsets(imageCombiner: ImageCombiner) {
    // set statusBarHeightPx, navigationBarHeightPx, screenHeight for imageCombiner
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues()
    val density = LocalDensity.current
    val statusBarHeightPx =
        with(density) { statusBarPadding.calculateTopPadding().toPx().toInt() }
    val navigationBarHeightPx = with(density) {
        navigationBarPadding.calculateBottomPadding().toPx().toInt()
    }
    val displayMetrics = LocalContext.current.resources.displayMetrics
    val screenHeight = displayMetrics.heightPixels
    imageCombiner.setInsets(
        statusBarHeightPx,
        navigationBarHeightPx,
        screenHeight
    )
}
