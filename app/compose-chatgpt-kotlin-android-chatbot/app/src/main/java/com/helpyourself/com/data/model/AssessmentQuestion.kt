package com.helpyourself.com.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class AssessmentQuestion(
    val id: String = "",
    val text: String = "",
    val options: List<String> = emptyList(),
    val maxScore: Int = 0
) {
    // Empty constructor required by Firebase
    constructor() : this("", "", emptyList(), 0)
} 