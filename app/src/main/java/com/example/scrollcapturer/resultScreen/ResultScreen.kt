package com.example.scrollcapturer.resultScreen

import android.app.AlertDialog
import android.app.Dialog
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.scrollcapturer.stitchscreen.StitchScreenViewModel
import kotlinx.coroutines.launch

@Composable
fun ResultScreen(
    navController: NavController,
    stitchScreenViewModel: StitchScreenViewModel,
    resultScreenViewModel: ResultScreenViewModel = hiltViewModel()
) {
    val resultImageBitmap = stitchScreenViewModel.resultImageBitmap
    Text("welcome to result screen")
    ResultImage(resultImageBitmap)
    SaveButton(resultScreenViewModel, resultImageBitmap)
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
fun SaveButton(viewModel: ResultScreenViewModel, resultImageBitmap: ImageBitmap) {

    var showDialog by remember {
        mutableStateOf(false)
    }
    var customFileName by remember {
        mutableStateOf("stitch_image_0")
    }

    Button(onClick = {
        showDialog = true
    }) {
        Text("SAVE")
    }

    if (showDialog) {
        DialogTextField(
            onDismiss = { showDialog = false },
            textFieldValue = customFileName,
            onValueChange = { userInput ->
                customFileName = userInput
            },
            onSave = { viewModel.saveImageAndShowToast(resultImageBitmap, customFileName) }
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
        onDismissRequest = {
            focusManager.clearFocus()
            onDismiss()
        },
        title = { Text("Enter file name") },
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
                Button(onClick = {
                    onDismiss()
                }) {
                    Text("Cancel")
                }

                // ConfirmButton
                Button(
                    onClick = {
                        onSave()
                        onDismiss()
                    }
                ) {
                    Text("Save")
                }
            }
        }
    )
}