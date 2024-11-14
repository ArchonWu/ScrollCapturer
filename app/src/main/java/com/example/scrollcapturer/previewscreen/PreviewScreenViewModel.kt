package com.example.scrollcapturer.previewscreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scrollcapturer.ImageCombiner
import com.example.scrollcapturer.utils.ImageUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.opencv.core.Mat
import javax.inject.Inject

// handles the feature matching & stitching
@HiltViewModel
class PreviewScreenViewModel @Inject constructor(
    private val imageCombiner: ImageCombiner
) : ViewModel() {

    var resultImageBitmap by mutableStateOf(ImageBitmap(1, 1))
        private set

    private val tag = "StitchScreenViewModel"

    fun stitchAllImages(imageMats: List<Mat>): ImageBitmap {

        viewModelScope.launch(Dispatchers.IO) {

            // iteratively stitches two images in the list: do feature matching, and images stitching
            var resultStitchedImage = imageMats[0]
            for (i in 1 until imageMats.size) {
                resultStitchedImage = imageCombiner.stitchImage(resultStitchedImage, imageMats[i])
            }

            resultImageBitmap = ImageUtils.convertMatToBitmap(resultStitchedImage).asImageBitmap()

        }
        return resultImageBitmap
    }
}