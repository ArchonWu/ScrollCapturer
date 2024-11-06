package com.example.scrollcapturer

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
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

        if (OpenCVLoader.initLocal()) {
            Log.i("OpenCV", "OpenCV successfully loaded.")
        } else {
            Log.e("OpenCV", "OpenCV initialization failed.")
        }

        enableEdgeToEdge()
        setContent {
            ScrollCapturerTheme {
                Column(
//                    modifier = Modifier.background(Color.LightGray)
                )
                {
                    val navController = rememberNavController()
                    val sharedViewModel: ScreenshotListSharedViewModel = hiltViewModel()
                    val stitchScreenViewModel: StitchScreenViewModel = hiltViewModel()

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
