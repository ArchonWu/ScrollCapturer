package com.example.scrollcapturer.screenshotListScreen

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.scrollcapturer.services.ScreenCaptureService
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
                { AddPictureButton(imagePickerLauncher, sharedViewModel) },
                { AutoModeButton() },
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
fun AddPictureButton(
    imagePickerLauncher: ManagedActivityResultLauncher<String, List<Uri>>,
    sharedViewModel: ScreenshotListSharedViewModel
) {

    var isExpanded by remember {
        mutableStateOf(false)
    }

    StyledButton(
        text = "ADD",
        onClick = {
//            isExpanded = true
            imagePickerLauncher.launch("image/*")

        }

    )

//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//        if (isExpanded) {
//            StyledButton(
//                text = "GALLERY",
//                onClick = { imagePickerLauncher.launch("image/*") }
//            )
//
//            StyledButton(
//                text = "AUTO",
//                onClick = {}
//            )
//        }
//    }
}

@Composable
fun AutoModeButton() {
    val context = LocalContext.current
    val mediaProjectionManager = remember {
        context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }
    val screenCaptureIntent = mediaProjectionManager.createScreenCaptureIntent()

    val startMediaProjectionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {    // user granted the requested permission
            val startServiceIntent = Intent(context, ScreenCaptureService::class.java)
            startServiceIntent.action = ScreenCaptureService.Actions.START_PROJECTION.toString()
            startServiceIntent.putExtra("resultCode", result.resultCode)
            startServiceIntent.putExtra("data", result.data)
            context.startService(startServiceIntent)
        } else {    // no permission from user
            Toast.makeText(context, "Permission required for Auto mode", Toast.LENGTH_SHORT).show()
        }
    }

    StyledButton(
        text = "AUTO",
        onClick = {
            startMediaProjectionLauncher.launch(screenCaptureIntent)
        }
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
