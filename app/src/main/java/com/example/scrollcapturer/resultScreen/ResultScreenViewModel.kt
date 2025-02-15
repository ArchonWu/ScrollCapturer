package com.example.scrollcapturer.resultScreen

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scrollcapturer.ImageCombiner
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject


@HiltViewModel
class ResultScreenViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageCombiner: ImageCombiner
) : ViewModel() {

    private val _resultImageBitmap = MutableStateFlow<ImageBitmap?>(null)
    val resultImageBitmap: StateFlow<ImageBitmap?> = _resultImageBitmap.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun getResultImage() {
        // if from user-added images, use imageCombiner to stitch the image
        if (imageCombiner.getUserAddedImagesSize() > 0) {
            startStitching()
        } else {
            var progression = imageCombiner.getProgressions()
            var completed = progression[0]
            var total = progression[1]
            while (completed != total) {
                _isLoading.value = true
                progression = imageCombiner.getProgressions()
                completed = progression[0]
                total = progression[1]
            }
            _resultImageBitmap.value = imageCombiner.getResult()
            _isLoading.value = false
        }
    }

    private fun startStitching() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = withContext(Dispatchers.Default) {
                imageCombiner.stitchAllImages()
            }
            _resultImageBitmap.value = result
            _isLoading.value = false
        }
    }

    // get the downloads directory as default save directory
    private val downloadsDir: File =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

    init {
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }
    }

    fun saveImageAndShowToast(imageBitmap: ImageBitmap, fileName: String) {
        viewModelScope.launch {
            val savedFilePath = saveImageToStorage(imageBitmap, fileName)

            // Toast when finished
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Image saved to: $savedFilePath",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // file saving is run asynchronously, could result in returning invalid path to file
    // need to either use with callback or suspend fun
    private suspend fun saveImageToStorage(imageBitmap: ImageBitmap, fileName: String): String {
        // need to save as Bitmap format
        val bitmap: Bitmap = imageBitmap.asAndroidBitmap()

        val imageFile = File(downloadsDir, generateUniqueFileName(fileName))

        var fos: FileOutputStream? = null
        try {
            fos = withContext(Dispatchers.IO) {
                FileOutputStream(imageFile)
            }
            // use the compress method on the bitmap object to write image to the output stream
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                withContext(Dispatchers.IO) {
                    fos!!.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return imageFile.absolutePath
    }

    private fun generateUniqueFileName(fileName: String): String {
        var counter = 0
        var newFileName = "$fileName.png"
        while (File(downloadsDir, newFileName).exists()) {
            counter++
            newFileName = fileName + "_" + "($counter).png"
        }
        return newFileName
    }

}