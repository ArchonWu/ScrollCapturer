package com.example.scrollcapturer.previewscreen

import android.content.ContentResolver
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.scrollcapturer.R
import com.example.scrollcapturer.screenshotListScreen.ScreenshotListSharedViewModel
import com.example.scrollcapturer.ui.components.MenuBar
import com.example.scrollcapturer.ui.components.StyledButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    navController: NavController,
    sharedViewModel: ScreenshotListSharedViewModel,
    previewScreenViewModel: PreviewScreenViewModel,
) {

    val context = LocalContext.current
    val contentResolver = context.contentResolver

    val imageUriList = sharedViewModel.selectedImagesUri

    TopAppBar(
        title = {
            Text(text = "Combine Order Preview")
        }
    )

    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        if (imageUriList.isEmpty()) {
            Text(
                text = "No images were added",
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
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

    // Bottom MenuBar
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier.fillMaxSize()
    ) {
        MenuBar(
            buttons = listOf(
                { BackButton(navController) },
                {
                    StartStitchingButton(
                        navController,
                        contentResolver,
                        imageUriList,
                        previewScreenViewModel
                    )
                })
        )
    }
}

@Composable
fun BackButton(navController: NavController) {
    StyledButton(
        text = "Back",
        onClick = { navController.navigate("screenshot_list_screen") },
        resID = R.drawable.baseline_reply_24
    )
}

@Composable
fun StartStitchingButton(
    navController: NavController,
    contentResolver: ContentResolver,
    imageUriList: List<Uri>,
    previewScreenViewModel: PreviewScreenViewModel
) {
    StyledButton(text = "Combine", onClick = {
        previewScreenViewModel.handleCombine(imageUriList, contentResolver)
        navController.navigate("result_screen")
    }, resID = R.drawable.outline_content_cut_24)
}

//@Preview
//@Composable
//fun PreviewScreenPreview() {
//    val navController = rememberNavController()
//
//    val sharedViewModel = ScreenshotListSharedViewModel()
//    val previewScreenViewModel = PreviewScreenViewModel(
//        imageCombiner = imageCombiner
//    )
//
//    // Mock selected images URIs
//    sharedViewModel.selectedImagesUri = listOf(
//        Uri.parse("file://path/to/image1.jpg"),
//        Uri.parse("file://path/to/image2.jpg")
//    )
//
//    // Preview the PreviewScreen composable
//    PreviewScreen(
//        navController = navController,
//        sharedViewModel = sharedViewModel,
//        previewScreenViewModel = previewScreenViewModel
//    )
//}