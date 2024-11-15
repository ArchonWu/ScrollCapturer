package com.example.scrollcapturer.resultScreen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.scrollcapturer.ImageCombiner
import com.example.scrollcapturer.ui.components.MenuBar
import com.example.scrollcapturer.ui.components.StyledButton

@Composable
fun ResultScreen(
    navController: NavController,
    resultScreenViewModel: ResultScreenViewModel = hiltViewModel(),
    imageCombiner: ImageCombiner
) {
    val resultImageBitmap = imageCombiner.resultImageBitmap
    Log.d("result_screen", "$resultImageBitmap")

    ResultImage(resultImageBitmap)

    // Bottom MenuBar
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier.fillMaxSize()
    ) {
        MenuBar(
            buttons = listOf(
                { RestartButton(navController) },
                { SaveButton(resultScreenViewModel, resultImageBitmap) })
        )
    }
}

@Composable
fun ResultImage(resultImageBitmap: ImageBitmap) {
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .verticalScroll(rememberScrollState())
    )
    {
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
fun RestartButton(navController: NavController) {
    StyledButton(
        text = "RESTART",
        onClick = { navController.navigate("screenshot_list_screen") }
    )
}

@Composable
fun SaveButton(resultScreenViewModel: ResultScreenViewModel, resultImageBitmap: ImageBitmap) {
    var showDialog by remember {
        mutableStateOf(false)
    }
    var customFileName by remember {
        mutableStateOf("stitch_image_0")
    }

    StyledButton(
        text = "SAVE",
        onClick = { showDialog = true }
    )

    // AlertDialog for saving image
    if (showDialog) {
        DialogTextField(
            onDismiss = { showDialog = false },
            textFieldValue = customFileName,
            onValueChange = { userInput ->
                customFileName = userInput
            },
            onSave = {
                resultScreenViewModel.saveImageAndShowToast(
                    resultImageBitmap,
                    customFileName
                )
            }
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
    AlertDialog(
        shape = RoundedCornerShape(0.dp),
        onDismissRequest = {
            focusManager.clearFocus()
            onDismiss()
        },
        title = { Text("Enter file name:") },
        text = {
            Column {
                TextField(
                    value = textFieldValue,
                    onValueChange = onValueChange,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // DismissButton
                Button(
                    onClick = { onDismiss() }
                ) {
                    Text("CANCEL")
                }

                // ConfirmButton
                Button(
                    onClick = {
                        onSave()
                        onDismiss()
                    }
                ) {
                    Text("SAVE")
                }
            }
        }
    )
}