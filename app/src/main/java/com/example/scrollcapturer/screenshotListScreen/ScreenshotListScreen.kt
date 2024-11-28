package com.example.scrollcapturer.screenshotListScreen

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.scrollcapturer.services.ScreenCaptureService
import com.example.scrollcapturer.ui.theme.ScrollCapturerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenshotListScreen(
    screenshotListViewModel: ScreenshotListViewModel,
    onNextButtonClicked: () -> Unit = {},
    onRequestAccessibilityPermission: () -> Unit = {},
    onResetButtonClicked: () -> Unit = {},
    modifier: Modifier
) {

    val imagePickerLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetMultipleContents(),
            onResult = { uris: List<Uri> ->
                screenshotListViewModel.addImageUris(uris)
            })

    Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "Long Screenshot Capturer",
                    style = MaterialTheme.typography.headlineSmall
                )
            }, colors = TopAppBarDefaults.mediumTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }, bottomBar = {
        BottomAppBar(actions = {
            IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                Icon(
                    imageVector = Icons.Default.LibraryAdd, contentDescription = null
                )
            }
            IconButton(onClick = {
                screenshotListViewModel.resetImageUris()
                onResetButtonClicked()
            }) {
                Icon(
                    imageVector = Icons.Default.Refresh, contentDescription = null
                )
            }
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.Edit, contentDescription = null
                )
            }
        }, floatingActionButton = {
            if (screenshotListViewModel.selectedImagesUri.isEmpty()) {
                CaptureFloatingActionButton(onRequestAccessibilityPermission)
            } else {
                PreviewFloatingActionButton(onNextButtonClicked)
            }
        })
    }) { paddingValues ->
        Box(
            modifier = modifier.padding(paddingValues)
        ) {
            if (screenshotListViewModel.selectedImagesUri.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "Add some photos to start!",
                        style = MaterialTheme.typography.displayMedium,
                    )
                    // TODO: add a stitching icon
                }
            } else {

                Box(
                    contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()
                ) {
                    ScreenshotGrid(imageUriList = screenshotListViewModel.selectedImagesUri)
                }
            }
        }
    }

}

@Composable
fun ScreenshotGrid(imageUriList: List<Uri>) {
    LazyVerticalGrid(columns = GridCells.Fixed(5),   // 5 images per row
        modifier = Modifier.fillMaxSize(), content = {
            items(imageUriList) { uri ->
                ScreenshotSlot(uri = uri)
            }

        })
}

@Composable
fun ScreenshotSlot(uri: Uri) {
    Image(
        painter = rememberAsyncImagePainter(model = uri),
        contentDescription = "screenshot",
        contentScale = ContentScale.Crop,   // crop image to this size
        modifier = Modifier.size(120.dp)
    )
}

@Composable
fun CaptureFloatingActionButton(
    onRequestAccessibilityPermission: () -> Unit
) {
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

    var showPermissionDialog by remember { mutableStateOf(false) }
    var permissionGranted by remember { mutableStateOf(false) }

    FloatingActionButton(onClick = {
        if (permissionGranted) {
            startMediaProjectionLauncher.launch(
                screenCaptureIntent
            )
        } else {
            showPermissionDialog = true
        }
    }) {
        Icon(imageVector = Icons.Default.Cast, contentDescription = null)

        if (showPermissionDialog && !permissionGranted) {
            PermissionDialog(
                onDismiss = { showPermissionDialog = false },
                onGrantPermission = {
                    onRequestAccessibilityPermission()
                    permissionGranted = true
                    showPermissionDialog = false
                }
            )
        }
    }
}

@Composable
fun PermissionDialog(
    onDismiss: () -> Unit,
    onGrantPermission: () -> Unit
) {
    AlertDialog(shape = RoundedCornerShape(0.dp),
        onDismissRequest = onDismiss,
        title = { Text("Permission Required") },
        text = { Text("Accessibility permission is required for using auto mode") },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // DismissButton
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.LightGray, contentColor = Color.Black
                    )
                ) {
                    Text("Cancel")
                }

                // ConfirmButton
                Button(
                    onClick = onGrantPermission,
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.LightGray, contentColor = Color.Black
                    )
                ) {
                    Text("Grant Permission")
                }
            }
        })
}

@Composable
fun PreviewFloatingActionButton(onNextButtonClicked: () -> Unit) {
    FloatingActionButton(
        onClick = onNextButtonClicked
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null
        )
    }
}

@Preview
@Composable
fun ScreenshotListScreenPreview() {
    val previewScreenshotListViewModel = ScreenshotListViewModel().apply {
        addImageUris(
            listOf(
                Uri.parse(""),
                Uri.parse(""),
                Uri.parse(""),
                Uri.parse(""),
                Uri.parse(""),
                Uri.parse(""),
                Uri.parse("")
            )
        )
    }

    ScrollCapturerTheme {
        ScreenshotListScreen(
            screenshotListViewModel = previewScreenshotListViewModel,
            modifier = Modifier.fillMaxHeight(),
        )
    }
}

