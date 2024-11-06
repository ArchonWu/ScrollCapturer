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
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject


@HiltViewModel
class ResultScreenViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    // get the downloads directory
    val downloadsDir =
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
            // use the compress method on the BitMap object to write image to the OutputStream
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