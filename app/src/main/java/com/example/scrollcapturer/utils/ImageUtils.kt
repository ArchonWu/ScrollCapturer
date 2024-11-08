package com.example.scrollcapturer.utils

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.net.Uri
import androidx.core.graphics.createBitmap
import org.opencv.android.Utils
import org.opencv.core.Mat


object ImageUtils {

    fun convertMatToBitmap(mat: Mat): Bitmap {
        val bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, bitmap)
        return bitmap
    }

    fun convertUrisToMats(
        imageUris: List<Uri>,
        contentResolver: ContentResolver
    ): MutableList<Mat> {
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

    fun convertImageToBitmap(image: Image): Bitmap {

        val buffer = image.planes[0].buffer
//        val bytes = ByteArray(buffer.remaining())
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)

        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
        return bitmap
    }

}