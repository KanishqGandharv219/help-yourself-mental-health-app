package com.helpyourself.com.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "depression_answers")
data class DepressionAnswerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val questionId: Int,
    val question: String,
    val answer: String,
    val timestamp: Long = System.currentTimeMillis(),
    val date: String
) 