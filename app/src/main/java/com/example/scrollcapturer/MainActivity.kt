package com.example.scrollcapturer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.scrollcapturer.ui.theme.ScrollCapturerTheme
import androidx.navigation.compose.rememberNavController
import com.example.scrollcapturer.screenshotListScreen.ScreenshotListScreen
import com.example.scrollcapturer.stitchscreen.StitchScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Greeting(name = "Pretty")

            val navController = rememberNavController()
            NavHost (navController = navController, startDestination = "screenshot_list_screen") {
                composable("screenshot_list_screen"){
                    ScreenshotListScreen(navController)
                }
                composable("stitch_screen"){
                    StitchScreen(navController)
                }
            }
//            ScrollCapturerTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Greeting(
//                        name = "Android",
//                        modifier = Modifier.padding(innerPadding)
//                    )
//                }
//            }
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ScrollCapturerTheme {
        Greeting("Android")
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