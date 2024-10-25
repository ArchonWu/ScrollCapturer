package com.example.scrollcapturer.stitchscreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.scrollcapturer.screenshotListScreen.ScreenshotListSharedViewModel

@Composable
fun StitchScreen(navController: NavController, viewModel: ScreenshotListSharedViewModel) {
    Text(
        "STITCH ORDER PREVIEW",
        fontSize = 18.sp,
        modifier = Modifier.shadow(2.dp)
    )

    val imageUriList = viewModel.selectedImagesUri

    Box (
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn {
            items(imageUriList) { uri ->
                Image(
                    painter = rememberAsyncImagePainter(model = uri),
                    contentDescription = "screenshot",
                    modifier = Modifier
                        .size(400.dp)
                )
            }
        }
    }
}
