package com.example.scrollcapturer.resultScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.scrollcapturer.ImageCombiner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    resultScreenViewModel: ResultScreenViewModel = hiltViewModel(),
    imageCombiner: ImageCombiner,
    modifier: Modifier
) {
    val resultImageBitmap = imageCombiner.resultImageBitmap
    var showDialog by remember {
        mutableStateOf(false)
    }
    var customFileName by remember {
        mutableStateOf("stitch_image_0")
    }

    Scaffold(modifier = modifier.fillMaxSize(), topBar = {
        TopAppBar(
            title = {
                Text(
                    text = "Result",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            colors = TopAppBarDefaults.mediumTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            navigationIcon = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null
                    )
                }
            }
        )
    }, bottomBar = {
        BottomAppBar(
            actions = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null
                    )
                }
            }, floatingActionButton = {
                FloatingActionButton(onClick = { showDialog = true }) {
                    Icon(
                        imageVector = Icons.Filled.Save, contentDescription = null
                    )
                }
            })

    }) { paddingValues ->
        Box(modifier = modifier.padding(paddingValues)) {
            ResultImage(resultImageBitmap)
        }

        // AlertDialog for saving image
        if (showDialog) {
            DialogTextField(onDismiss = { showDialog = false },
                textFieldValue = customFileName,
                onValueChange = { userInput ->
                    customFileName = userInput
                },
                onSave = {
                    resultScreenViewModel.saveImageAndShowToast(
                        resultImageBitmap, customFileName
                    )
                })
        }
    }
}

@Composable
fun ResultImage(resultImageBitmap: ImageBitmap) {
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        Image(
            bitmap = resultImageBitmap,
            contentDescription = "result of stitching",
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
        )
    }
}

@Composable
fun DialogTextField(
    onDismiss: () -> Unit,
    textFieldValue: String,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    AlertDialog(shape = RoundedCornerShape(0.dp), onDismissRequest = {
        focusManager.clearFocus()
        onDismiss()
    }, title = { Text("Enter file name:") }, text = {
        Column {
            TextField(
                value = textFieldValue,
                onValueChange = onValueChange,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }, confirmButton = {
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // DismissButton
            Button(
                onClick = { onDismiss() },
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.LightGray, contentColor = Color.Black
                )
            ) {
                Text("Back")
            }

            // ConfirmButton
            Button(
                onClick = {
                    onSave()
                    onDismiss()
                }, shape = RoundedCornerShape(0.dp), colors = ButtonDefaults.buttonColors(
                    containerColor = Color.LightGray, contentColor = Color.Black
                )
            ) {
                Text("Save")
            }
        }
    })
}