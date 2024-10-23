package com.example.scrollcapturer.screenshotListScreen

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ScreenshotListViewModel : ViewModel() {
    var selectedImagesUri: List<Uri> by mutableStateOf(emptyList())
        private set

    fun addImageUris(addUris: List<Uri>) {
        viewModelScope.launch {
            selectedImagesUri = selectedImagesUri + addUris
        }
    }

    fun deleteImageUri(deleteUri: Uri) {
        viewModelScope.launch {
            selectedImagesUri = selectedImagesUri - deleteUri
        }
    }

}