package com.helpyourself.com.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.helpyourself.com.data.local.entities.DepressionResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DepressionResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: DepressionResultEntity)

    @Query("SELECT * FROM depression_results ORDER BY timestamp DESC")
    fun getAllResults(): Flow<List<DepressionResultEntity>>
} 