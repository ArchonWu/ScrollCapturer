package com.example.scrollcapturer.screenshotListScreen

import android.net.Uri
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.scrollcapturer.ui.components.MenuBar
import com.example.scrollcapturer.ui.components.StyledButton

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
    }

    // Bottom MenuBar
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier.fillMaxSize()
    ) {
        MenuBar(
            buttons = listOf(
                { AddPictureButton(imagePickerLauncher) },
                { RemovePictureButton() },
                { ResetPictureButton() },
                { NextButton(navController) },
            )
        )
    }
}

@Composable
fun ScreenshotGrid(imageUriList: List<Uri>) {
    Log.d("ScreenshotGrid", "GRID IS CALLED: $imageUriList")
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),   // 5 images per row
        modifier = Modifier
            .fillMaxSize(),
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
            .size(120.dp)
    )
}

@Composable
fun AddPictureButton(imagePickerLauncher: ManagedActivityResultLauncher<String, List<Uri>>) {
    StyledButton(
        text = "ADD",
        onClick = { imagePickerLauncher.launch("image/*") }
    )

    // TODO: select from gallery or Auto mode
}

@Composable
fun RemovePictureButton() {
    StyledButton(
        text = "REMOVE",
        onClick = {}
    )
}

@Composable
fun ResetPictureButton() {
    StyledButton(
        text = "RESET",
        onClick = {}
    )
}

@Composable
fun NextButton(navController: NavController) {
    StyledButton(
        text = "NEXT",
        onClick = { navController.navigate("stitch_screen") }
    )
}
