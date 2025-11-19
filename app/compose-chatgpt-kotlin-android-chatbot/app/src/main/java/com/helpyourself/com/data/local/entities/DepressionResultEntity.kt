package com.helpyourself.com.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "depression_results")
data class DepressionResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val answersEncoded: String, // Encoded answers map
    val interpretation: String,
    val recommendation: String
) 