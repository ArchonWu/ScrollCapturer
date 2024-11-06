package com.example.scrollcapturer.resultScreen

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.os.Environment
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject


@HiltViewModel
class ResultScreenViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    fun saveImageToStorage(imageBitmap: ImageBitmap): String {
        // need to save as Bitmap format
        val bitmap: Bitmap = imageBitmap.asAndroidBitmap()

        // get the downloads directory
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }

        val imageFile = File(downloadsDir, "stitched_image.png")

        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(imageFile)
            // use the compress method on the BitMap object to write image to the OutputStream
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return imageFile.absolutePath
    }

    private fun generateUniqueFileName(fileName: String): String {
        return ""
    }

}