package com.example.scrollcapturer.resultScreen

import android.graphics.Paint.Align
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.content.MediaType.Companion.Text
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.scrollcapturer.stitchscreen.StitchScreenViewModel

@Composable
fun ResultScreen(navController: NavController, viewModel: StitchScreenViewModel) {
    val resultImageBitmap = viewModel.resultImageBitmap
    Text("welcome to result screen")
    ResultImage(resultImageBitmap)
    SaveButton()
}

@Composable
fun ResultImage(resultImageBitmap: ImageBitmap) {
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier
//            .fillMaxSize()
            .verticalScroll(rememberScrollState())
//            .border(2.dp, color = Color.Green)
    )
    {
        Image(
            bitmap = resultImageBitmap,
            contentDescription = "result of stitching",
            modifier = Modifier
//                .fillMaxSize()
//                .fillMaxHeight()
                .fillMaxWidth()
//                .align(Alignment.Center)
                .border(width = 2.dp, color = Color.Yellow)
        )
    }
}

@Composable
fun SaveButton() {
    Button(onClick = {}) {
        Text("SAVE")
    }
}