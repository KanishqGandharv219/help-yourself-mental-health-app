package com.helpyourself.com.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.helpyourself.com.data.local.entities.StressAnswerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StressAnswerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswer(answer: StressAnswerEntity)

    @Query("SELECT * FROM stress_answers WHERE date = :date")
    fun getAnswersForDate(date: String): Flow<List<StressAnswerEntity>>

    @Query("DELETE FROM stress_answers WHERE date = :date")
    suspend fun deleteAnswersByDate(date: String)
} 