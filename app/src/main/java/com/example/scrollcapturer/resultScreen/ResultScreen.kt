package com.example.scrollcapturer.resultScreen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.navigation.NavController
import com.example.scrollcapturer.stitchscreen.StitchScreenViewModel

@Composable
fun ResultScreen(
    navController: NavController,
    stitchScreenViewModel: StitchScreenViewModel,
    resultScreenViewModel: ResultScreenViewModel
) {
    val resultImageBitmap = stitchScreenViewModel.resultImageBitmap
    Text("welcome to result screen")
    ResultImage(resultImageBitmap)
    SaveButton(resultScreenViewModel, resultImageBitmap)
}

@Composable
fun ResultImage(resultImageBitmap: ImageBitmap) {
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .verticalScroll(rememberScrollState())
    )
    {
        Image(
            bitmap = resultImageBitmap,
            contentDescription = "result of stitching",
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
        )
    }
}

@Composable
fun SaveButton(viewModel: ResultScreenViewModel, resultImageBitmap: ImageBitmap) {
    Button(onClick = {
        val savePath = viewModel.saveImageToStorage(resultImageBitmap)
        Log.d("SaveButton", savePath)
    }) {
        Text("SAVE")
    }
}