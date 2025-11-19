package com.helpyourself.com.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.helpyourself.com.data.local.entities.DepressionAnswerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DepressionAnswerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswers(answers: List<DepressionAnswerEntity>)

    @Query("SELECT * FROM depression_answers WHERE date = :date")
    fun getAnswersForDate(date: String): Flow<List<DepressionAnswerEntity>>

    @Query("DELETE FROM depression_answers WHERE date = :date")
    suspend fun deleteAnswersByDate(date: String)
} 