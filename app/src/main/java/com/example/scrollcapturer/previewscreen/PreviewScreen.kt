package com.example.scrollcapturer.previewscreen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardReturn
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.scrollcapturer.Routes
import com.example.scrollcapturer.screenshotListScreen.ScreenshotListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    navController: NavController,
    sharedViewModel: ScreenshotListViewModel,
    previewScreenViewModel: PreviewScreenViewModel,
    modifier: Modifier,
) {

    val context = LocalContext.current
    val contentResolver = context.contentResolver

    val imageUriList = sharedViewModel.selectedImagesUri

    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Combine Order Preview",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.Start.name) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.KeyboardReturn,
                            contentDescription = null
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = null
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.ZoomOut,
                            contentDescription = null
                        )
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = {
                        Log.d("PREVIEW", "$imageUriList.size")
                        previewScreenViewModel.handleCombine(imageUriList, contentResolver)
                        navController.navigate(Routes.Result.name)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ContentCut,
                            contentDescription = null
                        )
                    }
                })
        }
    ) { paddingValues ->
        Box(
            modifier = modifier.padding(paddingValues)
        ) {

            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                if (imageUriList.isEmpty()) {
                    Text(
                        text = "No images were added",
                        style = MaterialTheme.typography.displayMedium
                    )
                } else {
                    LazyColumn {
                        items(imageUriList) { uri ->
                            Image(
                                painter = rememberAsyncImagePainter(model = uri),
                                contentDescription = "screenshots order preview",
                                modifier = Modifier
                                    .size(400.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
