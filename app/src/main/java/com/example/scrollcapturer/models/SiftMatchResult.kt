package com.example.scrollcapturer.models

import org.opencv.core.MatOfDMatch
import org.opencv.core.MatOfKeyPoint

class SiftMatchResult(
    val goodMatches: MatOfDMatch,
    val keypoints1: MatOfKeyPoint,
    val keypoints2: MatOfKeyPoint
)