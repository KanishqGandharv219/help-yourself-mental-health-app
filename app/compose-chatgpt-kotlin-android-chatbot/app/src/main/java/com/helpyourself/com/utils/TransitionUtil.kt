package com.helpyourself.com.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Point
import androidx.appcompat.app.AppCompatDelegate
import com.helpyourself.com.ui.theme.TransitionActivity

/**
 * Utility class for theme transition handling
 */
object TransitionUtil {
    // Store the screenshot bitmap during transition
    var transitionBitmap: Bitmap? = null
    
    // Track current theme state
    var isDarkMode: Boolean
        get() = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        private set(value) {}
    
    /**
     * Update the theme based on dark mode setting
     */
    fun updateTheme(darkMode: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (darkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
    
    /**
     * Switch to a new theme with animation
     */
    fun switchToTheme(activity: Activity, darkMode: Boolean, centerPoint: Point) {
        // Start the transition activity
        val intent = TransitionActivity.getIntent(activity, darkMode, centerPoint)
        activity.startActivity(intent)
        activity.overridePendingTransition(0, 0) // No animation for activity transition
    }
} 