package com.example.scrollcapturer.stitchscreen

import android.content.ContentResolver
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.scrollcapturer.screenshotListScreen.ScreenshotListSharedViewModel
import com.example.scrollcapturer.ui.components.MenuBar
import com.example.scrollcapturer.ui.components.StyledButton

@Composable
fun StitchScreen(
    navController: NavController,
    sharedViewModel: ScreenshotListSharedViewModel,
    viewModel: StitchScreenViewModel
) {

    val context = LocalContext.current
    val contentResolver = context.contentResolver

    val imageUriList = sharedViewModel.selectedImagesUri
    val visualizeImageList = viewModel.visualizeImageList
    val flaggedImageList = viewModel.flaggedImageList

    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        if (imageUriList.isEmpty()) {
            Text(text = "NO IMAGES WERE ADDED")
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.DarkGray),
        contentAlignment = Alignment.BottomEnd
    ) {

    }

    // Bottom MenuBar
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier.fillMaxSize()
    ) {
        MenuBar(
            buttons = listOf(
                { BackButton(navController) },
                { StartStitchingButton(navController, viewModel, contentResolver, imageUriList) })
        )
    }
}

@Composable
fun BackButton(navController: NavController) {
    StyledButton(
        text = "BACK",
        onClick = { navController.navigate("screenshot_list_screen") }
    )
}

@Composable
fun StartStitchingButton(
    navController: NavController,
    viewModel: StitchScreenViewModel,
    contentResolver: ContentResolver,
    imageUriList: List<Uri>
) {
    StyledButton(text = "START STITCHING", onClick = {
        viewModel.stitchAllImages(imageUriList, contentResolver)
        navController.navigate("result_screen")
    })
}


// TODO: should be moved to a new screen
// visualize goodMatches
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .border(width = 6.dp, Color.Magenta),
//            contentAlignment = Alignment.TopEnd
//        ) {
//            if (visualizeImageList.isEmpty()) {
//                Text(text = "NO IMAGES WERE ADDED")
//            } else {
//                LazyColumn {
//                    items(visualizeImageList) { image ->
//                        Image(
//                            bitmap = image,
//                            contentDescription = "visualize good matches image",
//                            modifier = Modifier
//                                .size(400.dp)
//                        )
//                    }
//                }
//            }
//        }


// Display flagged images if there are too few goodMatches detected
// otherwise display a "no problem detected" text with a green tick
//    Box(
//        modifier = Modifier.fillMaxSize(),
//        contentAlignment = Alignment.TopEnd
//    ) {
//        if (imageUriList.isEmpty()) {
//            Text(text = "NO IMAGES WERE ADDED")
//        } else {
//            LazyColumn {
//                items(flaggedImageList) { flaggedImage ->
//                    Image(
//                        bitmap = flaggedImage,
//                        contentDescription = "flagged image",
//                        modifier = Modifier
//                            .size(400.dp)
//                    )
//                }
//            }
//        }
//    }
//    Box(
//        modifier = Modifier.fillMaxSize(),
//        contentAlignment = Alignment.BottomStart
//    ) {
//        Button(onClick = { }) {
//            Text("Preview")
//        }
//    }

