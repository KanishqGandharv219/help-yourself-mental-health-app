package com.helpyourself.com.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.helpyourself.com.data.local.entities.StressResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StressResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: StressResultEntity)

    @Query("SELECT * FROM stress_results ORDER BY timestamp DESC")
    fun getAllResults(): Flow<List<StressResultEntity>>
} 