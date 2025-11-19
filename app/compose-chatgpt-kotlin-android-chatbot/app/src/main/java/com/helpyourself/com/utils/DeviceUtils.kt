package com.helpyourself.com.utils

import android.content.Context
import android.provider.Settings
import java.util.UUID

/**
 * Utility class for device-related operations
 */
object DeviceUtils {
    /**
     * Get a unique device ID for user identification
     * Falls back to a random UUID if device ID is not available
     */
    fun getDeviceId(context: Context): String {
        return try {
            // Get the Android ID - a unique device identifier
            val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            
            // If Android ID is null, empty, or "9774d56d682e549c" (known bug on some devices), use UUID
            if (androidId.isNullOrEmpty() || androidId == "9774d56d682e549c") {
                // Create a persistent UUID
                val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                var uuid = sharedPrefs.getString("device_uuid", null)
                
                if (uuid == null) {
                    uuid = UUID.randomUUID().toString()
                    sharedPrefs.edit().putString("device_uuid", uuid).apply()
                }
                
                uuid
            } else {
                androidId
            }
        } catch (e: Exception) {
            // In case of any exception, fall back to a random UUID
            UUID.randomUUID().toString()
        }
    }
} 