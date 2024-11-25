package com.example.scrollcapturer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.scrollcapturer.previewscreen.PreviewScreen
import com.example.scrollcapturer.previewscreen.PreviewScreenViewModel
import com.example.scrollcapturer.resultScreen.ResultScreen
import com.example.scrollcapturer.resultScreen.ResultScreenViewModel
import com.example.scrollcapturer.screenshotListScreen.ScreenshotListScreen
import com.example.scrollcapturer.screenshotListScreen.ScreenshotListViewModel
import okhttp3.Route
import org.opencv.android.OpenCVLoader

@Composable
fun ScrollCapturerApp(
    navController: NavHostController,
    imageCombiner: ImageCombiner
) {

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        requestPermissions(context as MainActivity)
        initializeNotificationChannel(context)
        initializeOpenCV()
    }

    Surface {

        val sharedViewModel: ScreenshotListViewModel = hiltViewModel()
        val previewScreenViewModel: PreviewScreenViewModel = hiltViewModel()
        val resultScreenViewModel: ResultScreenViewModel = hiltViewModel()

        InitializeImageCombinerWindowInsets(imageCombiner)

        NavHost(
            navController = navController,
            startDestination = Routes.Start.name
        ) {
            composable(Routes.Start.name) {
                ScreenshotListScreen(
                    screenshotListViewModel = sharedViewModel,
                    onNextButtonClicked = { navController.navigate(Routes.Preview.name) },
                    modifier = Modifier
                )
            }
            composable(Routes.Preview.name) {
                PreviewScreen(
                    sharedViewModel = sharedViewModel,
                    previewScreenViewModel = previewScreenViewModel,
                    onNextButtonClicked = { navController.navigate(Routes.Result.name) },
                    onReturnButtonClicked = { navController.popBackStack(Routes.Start.name, inclusive = false) },
                    modifier = Modifier
                )
            }
            composable(Routes.Result.name) {
                ResultScreen(
                    resultScreenViewModel = resultScreenViewModel,
                    onReturnButtonClicked = { navController.popBackStack(Routes.Start.name, inclusive = false) },
                    modifier = Modifier
                )
            }
        }
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

// navigation routes
enum class Routes {
    Start,
    Preview,
    Result
}