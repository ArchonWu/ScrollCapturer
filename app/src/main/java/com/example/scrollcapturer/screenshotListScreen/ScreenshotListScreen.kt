package com.example.scrollcapturer.screenshotListScreen

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter

@Composable
fun ScreenshotListScreen(navController: NavController) {

    var selectedImagesUri by remember {
        mutableStateOf(listOf<Uri>())
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) {
        selectedImagesUri.apply {
            selectedImagesUri = selectedImagesUri + it
        }
    }

    Box(
        contentAlignment = Alignment.TopEnd,
        modifier = Modifier
            .fillMaxSize()
    ) {
        ScreenshotGrid(imageUriList = selectedImagesUri)
        ExpandingMenu(imagePickerLauncher)
    }
}

@Composable
fun ScreenshotGrid(imageUriList: List<Uri>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),   // 5 images per row
        contentPadding = PaddingValues(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(imageUriList) { uri ->
            ScreenshotSlot(uri = uri)
        }
    }
}

@Composable
fun ScreenshotSlot(uri: Uri) {
    Image(
        painter = rememberAsyncImagePainter(model = uri),
        contentDescription = "screenshot",
        contentScale = ContentScale.Crop,   // crop image to this size
        modifier = Modifier
            .fillMaxWidth()
    )
}

@Composable
fun AddPictureButton(imagePickerLauncher: ManagedActivityResultLauncher<String, List<Uri>>) {
    Button(onClick = {
        imagePickerLauncher.launch("image/*")
    }) {
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
fun ExpandingMenu(imagePickerLauncher: ManagedActivityResultLauncher<String, List<Uri>>) {

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
        Column {
            AddPictureButton(imagePickerLauncher)
            RemovePictureButton()
            StartStitchingButton()
        }
    }
}
