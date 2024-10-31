package com.example.scrollcapturer.stitchscreen

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import com.example.scrollcapturer.models.SiftMatchResult
import org.opencv.android.Utils
import org.opencv.core.DMatch
import org.opencv.core.Mat
import org.opencv.core.MatOfDMatch
import org.opencv.core.MatOfKeyPoint
import org.opencv.features2d.Features2d
import org.opencv.features2d.FlannBasedMatcher
import org.opencv.features2d.SIFT

// handles the feature matching & stitching
class StitchScreenViewModel : ViewModel() {

    var visualizeImageList by mutableStateOf<List<ImageBitmap>>(emptyList())
        private set

    var flaggedImageList by mutableStateOf<List<ImageBitmap>>(emptyList())
        private set

    fun stitchAllImages(imagesUri: List<Uri>, contentResolver: ContentResolver) {

        val imagesMats = convertUrisToMats(imagesUri, contentResolver)
        Log.d("StitchScreenViewModel", "finished conversion to mats: ${imagesMats.size}")

        // iteratively stitches two images in the list: do feature matching, and images stitching
        var stitchedImage = imagesMats[0]
        for (i in 1 until imagesMats.size) {
            stitchedImage = stitchImage(stitchedImage, imagesMats[i])
        }

    }

    // visualize goodMatches
    private fun visualizeMatches(
        matchResult: SiftMatchResult,
        imageMat1: Mat,
        imageMat2: Mat
    ): Bitmap {
        val goodMatches = matchResult.goodMatches
        val keypoints1 = matchResult.keypoints1
        val keypoints2 = matchResult.keypoints2
        val resultImage = Mat()

        // check if keypoints and goodMatches are valid
        if (keypoints1.empty() || keypoints2.empty() || goodMatches.empty()) {
            return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        }

        // Draw matches using the actual keypoints
        Features2d.drawMatches(
            imageMat1,
            keypoints1,
            imageMat2,
            keypoints2,
            goodMatches,
            resultImage
        )

        // Convert the resulting Mat to Bitmap
        val resultImageBitmap = convertMatToBitmap(resultImage)
        resultImage.release()

        return resultImageBitmap
    }


    // perform the stitching (combining two images based on their good feature matches)
    private fun stitchImage(imageMat1: Mat, imageMat2: Mat): Mat {
        val siftMatchResult = siftFeatureMatching(imageMat1, imageMat2)
        val goodMatches: MatOfDMatch = siftMatchResult.goodMatches

        // visualize the good matches
        val visualizedMatchesBitmap =
            visualizeMatches(siftMatchResult, imageMat1, imageMat2)
        val visualizedMatchesImageBitmap = visualizedMatchesBitmap.asImageBitmap()
        visualizeImageList = visualizeImageList + visualizedMatchesImageBitmap

        // apply transformation based on good matches and stitch images together

        return imageMat1
    }

    // https://docs.opencv.org/4.x/d5/d6f/tutorial_feature_flann_matcher.html
    // detect the keypoints using SIFT Detector, and compute the descriptors
    private fun siftFeatureMatching(imageMat1: Mat, imageMat2: Mat): SiftMatchResult {
        val siftDetector: SIFT = SIFT.create()
        val keypoints1 = MatOfKeyPoint()
        val descriptors1 = Mat()
        val keypoints2 = MatOfKeyPoint()
        val descriptors2 = Mat()

        // TODO: feature matching only needs to be done on bottom half / top half of images
        siftDetector.detectAndCompute(imageMat1, Mat(), keypoints1, descriptors1)
        siftDetector.detectAndCompute(imageMat2, Mat(), keypoints2, descriptors2)
        Log.d("StitchScreenViewModel", "Detected ${keypoints1.size()} keypoints in image 1.")
        Log.d("StitchScreenViewModel", "Detected ${keypoints2.size()} keypoints in image 2.")

        // use FLANN-based matcher for matching descriptors
        val flannMatcher = FlannBasedMatcher.create()
        val knnMatches = mutableListOf<MatOfDMatch>()
        flannMatcher.knnMatch(descriptors1, descriptors2, knnMatches, 2)

        // filter matches using the Lowe's ratio test
        val ratioThresh = 0.3f
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

        return SiftMatchResult(goodMatches, keypoints1, keypoints2)
    }

    private fun convertMatToBitmap(mat: Mat): Bitmap {
        val bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, bitmap)
        return bitmap
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