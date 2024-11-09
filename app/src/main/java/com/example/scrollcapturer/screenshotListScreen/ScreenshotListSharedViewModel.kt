package com.example.scrollcapturer.screenshotListScreen

import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.projection.MediaProjection
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScreenshotListSharedViewModel @Inject constructor() : ViewModel() {
    var selectedImagesUri: List<Uri> by mutableStateOf(emptyList())
        private set

    var mediaProjection: MediaProjection? = null

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