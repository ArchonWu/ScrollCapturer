package com.example.scrollcapturer.stitchscreen

import android.graphics.Bitmap
import android.content.ContentResolver
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import org.opencv.android.Utils
import org.opencv.core.Mat

// handles the feature matching & stitching
class StitchScreenViewModel : ViewModel() {

    fun stitchAllImages(imagesUri: List<Uri>, contentResolver: ContentResolver) {

        val imagesMats = convertUrisToMats(imagesUri, contentResolver)

        Log.d("StitchScreenViewModel", "finished conversion to mats: ${imagesMats.size}")

        // for every two images in order:
        // do feature matching, and images stitching
        // feature matching only needs to be on bottom half / top half
    }

    private fun convertUrisToMats(imageUris: List<Uri>, contentResolver: ContentResolver): MutableList<Mat> {
        val imageMats = mutableListOf<Mat>()

        // convert to bitmaps
        for (uri in imageUris) {
            val bitmap = convertUriToBitmap(uri, contentResolver)

            if (bitmap != null) {
                // convert to mats
                val mat = Mat()
                Utils.bitmapToMat(bitmap, mat)
                imageMats.add(mat)
            }
        }

        return imageMats
    }

    private fun convertUriToBitmap(uri: Uri, contentResolver: ContentResolver): Bitmap? {
//        try {
//            val bitmap: Bitmap =
//                MediaStore.Images.Media.getBitmap(contentResolver, uri);  // deprecated
//            return bitmap
//        } catch (e: Exception) {
//            e.printStackTrace()
//            return null
//        }

        return try {
            val inputStream = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}