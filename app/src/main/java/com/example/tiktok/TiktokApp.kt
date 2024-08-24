package com.example.tiktok

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TiktokApp : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}