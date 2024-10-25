package com.example.scrollcapturer.repository

import android.graphics.Bitmap
import org.opencv.android.Utils.bitmapToMat

class CapturedImagesRepository {

    fun stitchImages(images: List<Bitmap>): Bitmap? {

        if (images.isEmpty()) return null

        val mats = images.map { bitmapToMat(it, null) }

        return null
    }
}