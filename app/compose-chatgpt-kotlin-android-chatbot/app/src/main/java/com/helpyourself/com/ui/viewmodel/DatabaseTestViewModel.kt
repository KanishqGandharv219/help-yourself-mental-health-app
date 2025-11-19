package com.helpyourself.com.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helpyourself.com.data.model.MentalHealthMetric
import com.helpyourself.com.data.repository.MentalHealthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Random
import javax.inject.Inject

@HiltViewModel
class DatabaseTestViewModel @Inject constructor(
    private val repository: MentalHealthRepository
) : ViewModel() {

    suspend fun testConnection(): Boolean = withContext(viewModelScope.coroutineContext + Dispatchers.IO) {
        repository.testDatabaseConnection()
    }

    suspend fun saveDummyMetric(): Unit = withContext(viewModelScope.coroutineContext + Dispatchers.IO) {
        val random = Random()
        val metric = MentalHealthMetric(
            depression = random.nextInt(11).toFloat(),
            anxiety = random.nextInt(11).toFloat(),
            stress = random.nextInt(11).toFloat()
        )
        repository.saveMentalHealthMetric(metric)
    }

    suspend fun fetchLatestMetric(): MentalHealthMetric? = withContext(viewModelScope.coroutineContext + Dispatchers.IO) {
        repository.getLatestMetric()
    }
} 