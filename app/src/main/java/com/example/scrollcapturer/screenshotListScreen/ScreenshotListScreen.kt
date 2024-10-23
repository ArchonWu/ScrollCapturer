package com.example.scrollcapturer.screenshotListScreen

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter

@Composable
fun ScreenshotListScreen(navController: NavController) {
    Box(
        contentAlignment = Alignment.TopEnd,
        modifier = Modifier
            .fillMaxSize()
//            .border(width = 2.dp, color = Color.Cyan)
    ) {
        ExpandingMenu()
    }
}

@Composable
fun ScreenshotEntry(uri: Uri) {
    Image(
        painter = rememberAsyncImagePainter(model = uri),
        contentDescription = "Screenshot",
        contentScale = ContentScale.Crop,   // crop image to this size
        modifier = Modifier
            .fillMaxWidth()
    )
}

@Composable
fun ScreenshotRow() {

}

@Composable
fun AddPictureButton() {
    Button(onClick = {}) {
        Text("+")
    }
}

@Composable
fun RemovePictureButton() {
    Button(onClick = {}) {
        Text("-")
    }
}

@Composable
fun StartStitchingButton() {
    Button(onClick = {}) {
        Text("STITCH")
    }
}

@Composable
fun ExpandingMenu() {
    var expanded by remember {
        mutableStateOf(false)
    }

        // the "3 dots" button
        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options",
            )
        }

        // Display vertical column with 3 buttons
        if (expanded) {
            Column() {
                AddPictureButton()
                RemovePictureButton()
                StartStitchingButton()
            }
        }
}
