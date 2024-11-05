package com.example.scrollcapturer

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ScrollCapturerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}