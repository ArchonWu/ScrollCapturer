package com.example.scrollcapturer.screenshotListScreen

import android.net.Uri
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter

@Composable
fun ScreenshotListScreen(
    navController: NavController,
    sharedViewModel: ScreenshotListSharedViewModel
) {

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris: List<Uri> ->
            sharedViewModel.addImageUris(uris)
            uris.forEach { uri ->
                Log.d("ScreenshotListScreen_uri", "Image added: $uri")
            }
        }
    )

    Box(
        contentAlignment = Alignment.TopEnd,
        modifier = Modifier
            .fillMaxSize()
    ) {
        ScreenshotGrid(imageUriList = sharedViewModel.selectedImagesUri)
        ExpandingMenu(navController, imagePickerLauncher)
    }
}

@Composable
fun ScreenshotGrid(imageUriList: List<Uri>) {
    Log.d("ScreenshotGrid", "GRID IS CALLED: $imageUriList")
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),   // 5 images per row
        modifier = Modifier
            .fillMaxSize()
            .border(width = 2.dp, color = Color.Red),
        content = {
            items(imageUriList) { uri ->
                ScreenshotSlot(uri = uri)
            }

        }
    )
}

@Composable
fun ScreenshotSlot(uri: Uri) {
    Image(
        painter = rememberAsyncImagePainter(model = uri),
        contentDescription = "screenshot",
        contentScale = ContentScale.Crop,   // crop image to this size
        modifier = Modifier
            .border(width = 2.dp, color = Color.Green)
            .size(100.dp)
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
fun StartStitchingButton(navController: NavController) {
    Button(onClick = {
        navController.navigate("stitch_screen")
    }) {
        Text("STITCH")
    }
}

@Composable
fun ExpandingMenu(
    navController: NavController,
    imagePickerLauncher: ManagedActivityResultLauncher<String, List<Uri>>
) {

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
            StartStitchingButton(navController)
        }
    }
}
