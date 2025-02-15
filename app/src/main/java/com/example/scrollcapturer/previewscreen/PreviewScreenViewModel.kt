package com.example.scrollcapturer.previewscreen

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scrollcapturer.ImageCombiner
import com.example.scrollcapturer.utils.ImageUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

// handles the feature matching & stitching
@HiltViewModel
class PreviewScreenViewModel @Inject constructor(
    private val imageCombiner: ImageCombiner
) : ViewModel() {

    private val tag = "StitchScreenViewModel"

    fun addImagesToCombiner(imageUriList: List<Uri>, contentResolver: ContentResolver) {
        Log.d(tag, imageUriList.size.toString())

        viewModelScope.launch(Dispatchers.IO) {
            val imageMatList = ImageUtils.convertUrisToMats(imageUriList, contentResolver)
            imageCombiner.addScreenshotsFromMats(imageMatList)
        }

    }

    // TODO: implement some kind of removing images via clicking

}