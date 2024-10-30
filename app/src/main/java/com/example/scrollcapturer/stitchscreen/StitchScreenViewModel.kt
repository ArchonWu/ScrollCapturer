package com.example.scrollcapturer.stitchscreen

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import org.opencv.android.Utils
import org.opencv.core.DMatch
import org.opencv.core.Mat
import org.opencv.core.MatOfDMatch
import org.opencv.core.MatOfKeyPoint
import org.opencv.features2d.FlannBasedMatcher
import org.opencv.features2d.SIFT

// handles the feature matching & stitching
class StitchScreenViewModel : ViewModel() {

    fun stitchAllImages(imagesUri: List<Uri>, contentResolver: ContentResolver) {

        val imagesMats = convertUrisToMats(imagesUri, contentResolver)

        Log.d("StitchScreenViewModel", "finished conversion to mats: ${imagesMats.size}")

        // iteratively stitches two images in the list:
        // do feature matching, and images stitching
        // feature matching only needs to be done on bottom half / top half
        var stitchedImage = imagesMats[0]
        for (i in 1 until imagesMats.size) {
            stitchedImage = stitchImage(stitchedImage, imagesMats[i])
        }

    }

    private fun stitchImage(imageMat1: Mat, imageMat2: Mat): Mat {
        val goodMatches: MatOfDMatch = siftFeatureMatching(imageMat1, imageMat2)

        // draw matches
        
        return Mat()
    }

    // https://docs.opencv.org/4.x/d5/d6f/tutorial_feature_flann_matcher.html
    // detect the keypoints using SIFT Detector, and compute the descriptors
    private fun siftFeatureMatching(imageMat1: Mat, imageMat2: Mat): MatOfDMatch {
        val siftDetector: SIFT = SIFT.create()
        val keypoints1 = MatOfKeyPoint()
        val descriptors1 = Mat()
        val keypoints2 = MatOfKeyPoint()
        val descriptors2 = Mat()

        siftDetector.detectAndCompute(imageMat1, Mat(), keypoints2, descriptors2)
        siftDetector.detectAndCompute(imageMat2, Mat(), keypoints1, descriptors1)
        Log.d("StitchScreenViewModel", "Detected ${keypoints1.size()} keypoints in image 1.")
        Log.d("StitchScreenViewModel", "Detected ${keypoints2.size()} keypoints in image 2.")

        // use FLANN-based matcher for matching descriptors
        val flannMatcher = FlannBasedMatcher.create()
        val knnMatches = mutableListOf<MatOfDMatch>()
        flannMatcher.knnMatch(descriptors1, descriptors2, knnMatches, 2)

        // filter matches using the Lowe's ratio test
        val ratioThresh = 0.6f
        val goodMatchesList = mutableListOf<DMatch>()
        for (match in knnMatches) {
            if (match.rows() > 1) {
                val matches = match.toArray()
                if (matches.size >= 2 && matches[0].distance < ratioThresh * matches[1].distance) {
                    goodMatchesList.add(matches[0])
                }
            }
        }
        val goodMatches = MatOfDMatch()
        goodMatches.fromList(goodMatchesList)
        Log.d("SIFT", "${goodMatches.size()}")

        return goodMatches
    }


    private fun convertUrisToMats(
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

}