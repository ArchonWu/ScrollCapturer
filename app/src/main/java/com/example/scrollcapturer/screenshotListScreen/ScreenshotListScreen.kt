package com.example.scrollcapturer.screenshotListScreen

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Forward
import androidx.compose.material.icons.filled.AddToPhotos
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
    screenshotListViewModel: ScreenshotListViewModel,
    modifier: Modifier
) {

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris: List<Uri> ->
            screenshotListViewModel.addImageUris(uris)
        }
    )

    Box(
        modifier = modifier     // with paddingValues from scaffold
    ) {
        if (screenshotListViewModel.selectedImagesUri.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Text(
                    text = "Add some photos to start!",
                    style = MaterialTheme.typography.displayMedium,
                )
                // TODO: add a stitching icon
            }
        } else {

            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                ScreenshotGrid(imageUriList = screenshotListViewModel.selectedImagesUri)
            }
        }

        // Bottom MenuBar
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier.fillMaxSize()
        ) {
            MenuBar(
                buttons = listOf(
                    { AddPictureButton(imagePickerLauncher) },
                    { ResetPictureButton(screenshotListViewModel) },
                    { AutoModeButton() },
                    { NextButton(navController) }
                )
            )
        }
    }

}

@Composable
fun ScreenshotGrid(imageUriList: List<Uri>) {
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
    imagePickerLauncher: ManagedActivityResultLauncher<String, List<Uri>>
) {
    StyledButton(
        text = "Add",
        onClick = {
            imagePickerLauncher.launch("image/*")
        },
        imageVector = Icons.Filled.AddToPhotos
    )
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
            Toast.makeText(context, "Permission required for Auto mode", Toast.LENGTH_SHORT)
                .show()
        }
    }

    StyledButton(
        text = "Auto",
        onClick = {
            startMediaProjectionLauncher.launch(screenCaptureIntent)
        },
        imageVector = Icons.Filled.CastConnected
    )
}

@Composable
fun ResetPictureButton(screenshotListViewModel: ScreenshotListViewModel) {
    StyledButton(
        text = "Reset",
        onClick = { screenshotListViewModel.resetImageUris() },
        imageVector = Icons.Filled.Refresh
    )
}

@Composable
fun NextButton(navController: NavController) {
    StyledButton(
        text = "Next",
        onClick = { navController.navigate("stitch_screen") },
        imageVector = Icons.AutoMirrored.Filled.Forward
    )
}
