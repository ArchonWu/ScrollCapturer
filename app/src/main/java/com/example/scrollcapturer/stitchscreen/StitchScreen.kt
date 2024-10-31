package com.example.scrollcapturer.stitchscreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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

@Composable
fun StitchScreen(
    navController: NavController,
    sharedViewModel: ScreenshotListSharedViewModel,
    viewModel: StitchScreenViewModel
) {

    val context = LocalContext.current
    val contentResolver = context.contentResolver

    Text(
        "STITCH ORDER PREVIEW",
        fontSize = 18.sp,
        modifier = Modifier.shadow(2.dp)
    )

    val imageUriList = sharedViewModel.selectedImagesUri
    val visualizeImageList = viewModel.visualizeImageList
    val flaggedImageList = viewModel.flaggedImageList


    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
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

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        Button(onClick = { viewModel.stitchAllImages(imageUriList, contentResolver) }) {
            Text("Start Stitching")
        }
    }
}
