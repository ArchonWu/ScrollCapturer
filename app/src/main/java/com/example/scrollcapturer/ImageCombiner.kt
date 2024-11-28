package com.example.scrollcapturer

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.scrollcapturer.models.SiftMatchResult
import com.example.scrollcapturer.utils.ImageUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.opencv.android.Utils
import org.opencv.calib3d.Calib3d
import org.opencv.calib3d.Calib3d.RANSAC
import org.opencv.core.Core
import org.opencv.core.DMatch
import org.opencv.core.Mat
import org.opencv.core.MatOfDMatch
import org.opencv.core.MatOfKeyPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.features2d.FlannBasedMatcher
import org.opencv.features2d.SIFT
import org.opencv.imgproc.Imgproc.warpPerspective

class ImageCombiner {

    private var statusBarHeightPx by mutableIntStateOf(0)
    private var navigationBarHeightPx by mutableIntStateOf(0)
    private var screenHeight by mutableIntStateOf(0)

    private var userAddedImages = mutableListOf<Bitmap>()
    private var userCombineResult: ImageBitmap? = null

    private var resultImageBitmap: ImageBitmap by mutableStateOf(ImageBitmap(1, 1))

    private val tag = "ImageCombiner"

    private var tempResult: Mat? = null
    private var totalCount: Int = 0
    private var completedCount: Int = 0

    fun getProgressions(): List<Int> {
        return listOf(totalCount, completedCount)
    }

    fun getResult(): ImageBitmap {
        if (totalCount == completedCount && tempResult != null) {
            this.resultImageBitmap = ImageUtils.convertMatToBitmap(tempResult!!).asImageBitmap()
        }
        return this.resultImageBitmap
    }

    fun processServiceCapturedImage(screenshotBitmap: Bitmap?) {

        if (tempResult == null && screenshotBitmap != null) {       // first image
            val capturedMat = ImageUtils.convertBitmapToMat(screenshotBitmap)
            tempResult = capturedMat
            totalCount++
            completedCount++
            Log.d(tag, "processServiceCapturedImage() count: $totalCount, ${tempResult!!.size()}")

        } else if (screenshotBitmap != null) {
            val capturedMat = ImageUtils.convertBitmapToMat(screenshotBitmap)
            tempResult = stitchImage(tempResult!!, capturedMat)
            totalCount++
            Log.d(tag, "processServiceCapturedImage() count: $totalCount, ${tempResult!!.size()}")

        } else {
            Log.d(tag, "processServiceCapturedImage(): $tempResult, $screenshotBitmap, $totalCount")
        }

    }

    fun stitchAllImages(): ImageBitmap {
        Log.d(tag, "stitchAllImages(), ${userAddedImages.size}")

        val serviceCapturedImageMats = ImageUtils.convertBitmapsToMats(userAddedImages)
        userCombineResult =
            ImageUtils.convertMatToBitmap(serviceCapturedImageMats[0]).asImageBitmap()

        // iteratively stitches two images in the list: do feature matching, and images stitching
        var resultStitchedImage = serviceCapturedImageMats[0]
        for (i in 1 until serviceCapturedImageMats.size) {
            resultStitchedImage = stitchImage(resultStitchedImage, serviceCapturedImageMats[i])
        }
        userCombineResult =
            ImageUtils.convertMatToBitmap(resultStitchedImage).asImageBitmap()

        clearServiceCapturedImages()
        Log.d(tag, "stitchAllImages(): finished")
        resultImageBitmap = userCombineResult as ImageBitmap

        return getResult()
    }

    // perform the stitching (combining two images based on their good feature matches)
    private fun stitchImage(imageMat1: Mat, imageMat2: Mat): Mat {
        Log.d("ImageCombiner", "start stitchImage: ${imageMat1.size()}, ${imageMat2.size()}")
        Log.d("ImageCombiner", "Insets: $statusBarHeightPx, $navigationBarHeightPx, $screenHeight")
        val siftMatchResult = siftFeatureMatching(imageMat1, imageMat2)

        // apply transformation based on good matches and stitch images together
        val homographyMatrix = calculateHomographyMatrix(siftMatchResult)

        if (homographyMatrix == null) {
            // combine two images without homography
            return simpleStitch(imageMat1, imageMat2)
        }

        // get the corners of imageMat2
        val corners = MatOfPoint2f(
            Point(0.0, 0.0),
            Point(imageMat2.cols().toDouble(), 0.0),
            Point(imageMat2.cols().toDouble(), imageMat2.rows().toDouble()),
            Point(0.0, imageMat2.rows().toDouble())
        )

        // project the corners of imageMat2 to imageMat1's coordinate space
        val projectedCorners = MatOfPoint2f()
        Core.perspectiveTransform(corners, projectedCorners, homographyMatrix)

        // extract projected corner points
        val projectedPoints = projectedCorners.toArray()

        // determine the bounding box of the projected corners
        val minX = projectedPoints.minOf { it.x }.toInt()
        val maxX = projectedPoints.maxOf { it.x }.toInt()
        val minY = projectedPoints.minOf { it.y }.toInt()
        val maxY = projectedPoints.maxOf { it.y }.toInt()

        // use the maximum y-coordinate to set the combined image height
        val targetHeight = maxY

        val resultImageMat = Mat(targetHeight, imageMat1.cols(), imageMat1.type())

        // warp image2 to align with image1 using the homography matrix
        warpPerspective(
            imageMat2,
            resultImageMat,
            homographyMatrix,
            Size(imageMat1.cols().toDouble(), targetHeight.toDouble())
        )

        val topIrrelevantRows = statusBarHeightPx
        val bottomIrrelevantRows = navigationBarHeightPx
        val rowsToCopy = imageMat1.rows() - bottomIrrelevantRows

        // place image1 onto the warped result
        imageMat1
            .submat(0, rowsToCopy, 0, imageMat1.cols())
            .copyTo(resultImageMat.submat(0, rowsToCopy, 0, imageMat1.cols()))

        completedCount++

        return resultImageMat
    }

    // stitch two images just by piecing them together vertically
    private fun simpleStitch(imageMat1: Mat, imageMat2: Mat): Mat {

        val rowsToCopy1 = imageMat1.rows() - navigationBarHeightPx
        val rowsToCopy2 = imageMat2.rows() - statusBarHeightPx

        val targetHeight =
            imageMat1.rows() + imageMat2.rows() - statusBarHeightPx - navigationBarHeightPx
        val resultImageMat = Mat(targetHeight, imageMat1.cols(), imageMat1.type())

        imageMat1
            .submat(0, rowsToCopy1, 0, imageMat1.cols())
            .copyTo(resultImageMat.submat(0, rowsToCopy1, 0, imageMat1.cols()))

        imageMat2
            .submat(0, rowsToCopy2, 0, imageMat2.cols())
            .copyTo(
                resultImageMat.submat(
                    rowsToCopy1, rowsToCopy1 + rowsToCopy2, 0, imageMat2.cols()
                )
            )

        return resultImageMat
    }

    // code adapted from opencv documentation:
    // https://docs.opencv.org/4.x/d7/dff/tutorial_feature_homography.html
    private fun calculateHomographyMatrix(siftMatchResult: SiftMatchResult): Mat? {

//        Log.d(tag, "${siftMatchResult.goodMatches.size()}")

        if (siftMatchResult.goodMatches.toList().size < 4) {
            // not enough matches, cannot calculate homography
            return null
        }

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

    // https://docs.opencv.org/4.x/d5/d6f/tutorial_feature_flann_matcher.html
    // detect the keypoints using SIFT Detector, and compute the descriptors
    private fun siftFeatureMatching(imageMat1: Mat, imageMat2: Mat): SiftMatchResult {
        val siftDetector: SIFT = SIFT.create()
        val keypoints1 = MatOfKeyPoint()
        val descriptors1 = Mat()
        val keypoints2 = MatOfKeyPoint()
        val descriptors2 = Mat()

        // feature matching only needs to be done on the bottom & top part of images
        // for image1: roi is 1/2 screenHeight of the bottom of the image, stops before navigationBarHeightPx
        // for image2: roi is 1/2 screenHeight of the top of the image, starts after statusBarHeightPx
        val roiImageMat1 =
            imageMat1.submat(
                imageMat1.rows() - screenHeight / 2,
                imageMat1.rows() - navigationBarHeightPx,
                0,
                imageMat1.cols()
            )
        val roiImageMat2 =
            imageMat2.submat(
                statusBarHeightPx,
                screenHeight / 2,
                0,
                imageMat2.cols()
            )
        siftDetector.detectAndCompute(roiImageMat1, Mat(), keypoints1, descriptors1)
        siftDetector.detectAndCompute(roiImageMat2, Mat(), keypoints2, descriptors2)

        // adjust coordinates of keypoints1 due to performing SIFT on only bottom part of image1
        val offsetY = imageMat1.rows() - screenHeight / 2 - statusBarHeightPx
        val adjustedKeypoints1 = keypoints1.toArray().map { keypoint ->
            keypoint.pt.y += offsetY
            keypoint
        }
        val adjustedMatOfKeypoints1 = MatOfKeyPoint()
        adjustedMatOfKeypoints1.fromList(adjustedKeypoints1)

        // use FLANN-based matcher for matching descriptors
        val flannMatcher = FlannBasedMatcher.create()
        val knnMatches = mutableListOf<MatOfDMatch>()
        flannMatcher.knnMatch(descriptors1, descriptors2, knnMatches, 2)

        // filter matches using the Lowe's ratio test
        val ratioThresh = 0.5f
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

        return SiftMatchResult(goodMatches, adjustedMatOfKeypoints1, keypoints2)
    }

    fun setInsets(statusBarHeight: Int, navigationBarHeight: Int, screenHeight: Int) {
        this.statusBarHeightPx = statusBarHeight
        this.navigationBarHeightPx = navigationBarHeight
        this.screenHeight = screenHeight
    }

    fun addScreenshot(screenshotBitmap: Bitmap) {
        userAddedImages += screenshotBitmap
    }

    fun clearServiceCapturedImages() {
        userAddedImages = mutableListOf<Bitmap>()
        resultImageBitmap = (ImageBitmap(1, 1))
    }

    // a one-time add all method
    fun addScreenshotsFromMats(imageMats: List<Mat>) {
        val bitmaps = imageMats.map { mat ->
            val bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(mat, bitmap)
            bitmap
        }
        userAddedImages += bitmaps
    }

    fun getUserAddedImagesSize(): Int {
        return userAddedImages.size
    }

    fun clearUserAddedImages() {
        userAddedImages = mutableListOf<Bitmap>()
    }

}