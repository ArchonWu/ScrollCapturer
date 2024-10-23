package com.example.scrollcapturer.screenshotListScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.lang.reflect.Modifier

@Composable
fun ScreenshotListScreen(navController: NavController) {
    Column {
        ExpandingMenu()
    }
}

@Composable
fun ScreenshotEntry() {

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
        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More options")
    }

    // Display vertical column with 3 buttons
    if (expanded) {
        Column(
        ) {
            AddPictureButton()
            RemovePictureButton()
            StartStitchingButton()
        }
    }
}
