package com.helpyourself.com.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stress_results")
data class StressResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val answersEncoded: String,
    val interpretation: String,
    val recommendation: String
) 