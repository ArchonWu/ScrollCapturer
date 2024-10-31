package com.example.scrollcapturer.stitchscreen

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import com.example.scrollcapturer.models.SiftMatchResult
import org.opencv.core.DMatch
import org.opencv.core.Mat
import org.opencv.core.MatOfDMatch
import org.opencv.core.MatOfKeyPoint
import org.opencv.features2d.Features2d
import org.opencv.features2d.FlannBasedMatcher
import org.opencv.features2d.SIFT
import com.example.scrollcapturer.utils.ImageUtils
import org.opencv.calib3d.Calib3d
import org.opencv.calib3d.Calib3d.RANSAC
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.warpPerspective

// handles the feature matching & stitching
class StitchScreenViewModel : ViewModel() {

    var visualizeImageList by mutableStateOf<List<ImageBitmap>>(emptyList())
        private set

    var flaggedImageList by mutableStateOf<List<ImageBitmap>>(emptyList())
        private set

    var resultImageBitmap by mutableStateOf(ImageBitmap(1, 1))
        private set

    fun stitchAllImages(imagesUri: List<Uri>, contentResolver: ContentResolver): ImageBitmap {

        val imagesMats = ImageUtils.convertUrisToMats(imagesUri, contentResolver)
        Log.d("StitchScreenViewModel", "finished conversion to mats: ${imagesMats.size}")

        // iteratively stitches two images in the list: do feature matching, and images stitching
        var resultStitchedImage = imagesMats[0]
        for (i in 1 until imagesMats.size) {
            resultStitchedImage = stitchImage(resultStitchedImage, imagesMats[i])
        }

        resultImageBitmap = ImageUtils.convertMatToBitmap(resultStitchedImage).asImageBitmap()
        return resultImageBitmap
    }

    // perform the stitching (combining two images based on their good feature matches)
    private fun stitchImage(imageMat1: Mat, imageMat2: Mat): Mat {
        val siftMatchResult = siftFeatureMatching(imageMat1, imageMat2)

        // apply transformation based on good matches and stitch images together
        val homographyMatrix = calculateHomographyMatrix(siftMatchResult)

        // adjust the target height to roughly 1.5 times the original, accommodating overlap
        val targetHeight = (imageMat1.rows() * 1.5).toInt()
        val resultImageMat = Mat()

        // warp image2 to align with image1 using the homography matrix
        warpPerspective(
            imageMat2,
            resultImageMat,
            homographyMatrix,
            Size(imageMat1.cols().toDouble(), targetHeight.toDouble())
        )

        val rowsToExclude = 100
        val rowsToCopy = imageMat1.rows() - rowsToExclude

        // place image1 onto the warped result to combine them, excluding the bottom few rows
        imageMat1.submat(0, rowsToCopy, 0, imageMat1.cols()).copyTo(resultImageMat.submat(0, rowsToCopy, 0, imageMat1.cols()))

        return resultImageMat
    }

    // code adapted from opencv documentation:
    // https://docs.opencv.org/4.x/d7/dff/tutorial_feature_homography.html
    private fun calculateHomographyMatrix(siftMatchResult: SiftMatchResult): Mat {

        // extract matched points
        val obj = mutableListOf<Point>()
        val scene = mutableListOf<Point>()
        val listOfKeypointsObject = siftMatchResult.keypoints1.toList()
        val listOfKeypointsScene = siftMatchResult.keypoints2.toList()
        val goodMatches: MatOfDMatch = siftMatchResult.goodMatches

        for (match in goodMatches.toList()) {
            obj.add(listOfKeypointsObject[match.queryIdx].pt)
            scene.add(listOfKeypointsScene[match.trainIdx].pt)
        }

        // create MatOfPoint2f for homography
        val objMat = MatOfPoint2f(*obj.toTypedArray())
        val sceneMat = MatOfPoint2f(*scene.toTypedArray())

        // calculate homography
        val homographyMatrix = Calib3d.findHomography(sceneMat, objMat, RANSAC, 5.0)
        return homographyMatrix
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
        val resultImageMat = Mat()

        // check if keypoints and goodMatches are valid
        if (keypoints1.empty() || keypoints2.empty() || goodMatches.empty()) {
            return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        }

        // draw matches using the actual keypoints
        Features2d.drawMatches(
            imageMat1,
            keypoints1,
            imageMat2,
            keypoints2,
            goodMatches,
            resultImageMat
        )

        val resultImageBitmap = ImageUtils.convertMatToBitmap(resultImageMat)
        resultImageMat.release()

        return resultImageBitmap
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

        return SiftMatchResult(goodMatches, keypoints1, keypoints2)
    }
}