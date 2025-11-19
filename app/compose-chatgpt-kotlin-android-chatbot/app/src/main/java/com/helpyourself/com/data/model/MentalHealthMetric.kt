package com.helpyourself.com.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class MentalHealthMetric(
    val userId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val depression: Float = 0f,
    val anxiety: Float = 0f,
    val stress: Float = 0f,
    val notes: String? = null
) {
    // Empty constructor required for Firebase
    constructor() : this("", 0L, 0f, 0f, 0f, null)
} 