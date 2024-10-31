package com.example.scrollcapturer

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.scrollcapturer.ui.theme.ScrollCapturerTheme
import androidx.navigation.compose.rememberNavController
import com.example.scrollcapturer.resultScreen.ResultScreen
import com.example.scrollcapturer.resultScreen.ResultScreenViewModel
import com.example.scrollcapturer.screenshotListScreen.ScreenshotListScreen
import com.example.scrollcapturer.screenshotListScreen.ScreenshotListSharedViewModel
import com.example.scrollcapturer.stitchscreen.StitchScreen
import com.example.scrollcapturer.stitchscreen.StitchScreenViewModel
import org.opencv.android.OpenCVLoader

class MainActivity : ComponentActivity() {
    private val screenshotListSharedViewModel: ScreenshotListSharedViewModel by viewModels()
    private val stitchScreenViewModel: StitchScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (OpenCVLoader.initLocal()) {
            Log.i("OpenCV", "OpenCV successfully loaded.");
        } else {
            Log.e("OpenCV", "OpenCV initialization failed.");
        }

        enableEdgeToEdge()
        setContent {
            ScrollCapturerTheme {
                Column(
//                    modifier = Modifier.background(Color.LightGray)
                )
                {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "screenshot_list_screen"
                    ) {
                        composable("screenshot_list_screen") {
                            ScreenshotListScreen(navController, screenshotListSharedViewModel)
                        }
                        composable("stitch_screen") {
                            StitchScreen(
                                navController, screenshotListSharedViewModel, stitchScreenViewModel
                            )
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


// maybe like a collapsible button (click on it to display more options)
// a "+" button for adding screenshots
// a "-" button for deleting screenshots
// a "start" button to start stitching

// a start screen to display all added photos
// a drag to sort added pictures function

// the actual stitching function

// a finish screen to display the final result of the stitched screen capture
// a back button

// floating app icon for auto scroll capture function