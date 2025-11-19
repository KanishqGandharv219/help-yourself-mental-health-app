package com.helpyourself.com.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.helpyourself.com.data.local.entities.AnxietyResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnxietyResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: AnxietyResultEntity)

    @Query("SELECT * FROM anxiety_results ORDER BY timestamp DESC")
    fun getAllResults(): Flow<List<AnxietyResultEntity>>
} 