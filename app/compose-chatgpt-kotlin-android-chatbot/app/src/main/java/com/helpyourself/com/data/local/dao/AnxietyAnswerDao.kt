package com.helpyourself.com.data.local.dao

import androidx.room.*
import com.helpyourself.com.data.local.entities.AnxietyAnswerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnxietyAnswerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswer(answer: AnxietyAnswerEntity)

    @Query("SELECT * FROM anxiety_answers WHERE date = :date")
    fun getAnswersForDate(date: String): Flow<List<AnxietyAnswerEntity>>

    @Query("SELECT SUM(score) FROM anxiety_answers WHERE date = :date")
    fun getTotalScoreForDate(date: String): Flow<Int>

    @Query("DELETE FROM anxiety_answers WHERE date = :date")
    suspend fun deleteAnswersByDate(date: String)
} 