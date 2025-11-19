package com.helpyourself.com

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CustomAIApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase services
        com.google.firebase.FirebaseApp.initializeApp(this)
    }
} 