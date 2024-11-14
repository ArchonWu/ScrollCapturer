package com.example.scrollcapturer.di

import android.content.Context
import com.example.scrollcapturer.ImageCombiner
import com.example.scrollcapturer.services.ScreenCaptureService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // automatically called by Hilt when request Int in ViewModel
    @Singleton
    @Provides
    fun provideScreenHeight(@ApplicationContext context: Context): Int {
        val displayMetrics = context.resources.displayMetrics
        return displayMetrics.heightPixels
    }

    @Singleton
    @Provides
    fun provideScreenCaptureService(): ScreenCaptureService {
        return ScreenCaptureService()
    }

    @Singleton
    @Provides
    fun provideImageCombiner(): ImageCombiner {
        return ImageCombiner()
    }
}