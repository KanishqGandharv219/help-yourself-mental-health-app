package com.helpyourself.com.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "anxiety_answers")
data class AnxietyAnswerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val questionId: Int,
    val question: String,
    val answer: String,
    val score: Int,
    val date: String,
    val timestamp: Long = System.currentTimeMillis()
) 