package com.helpyourself.com.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.helpyourself.com.data.local.dao.StressAnswerDao
import com.helpyourself.com.data.local.dao.StressResultDao
import com.helpyourself.com.data.local.entities.StressAnswerEntity
import com.helpyourself.com.data.local.entities.StressResultEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.helpyourself.com.data.repository.MentalHealthRepository
import com.helpyourself.com.data.model.MentalHealthMetric
import kotlinx.coroutines.flow.first

@HiltViewModel
class StressTestViewModel @Inject constructor(
    private val stressAnswerDao: StressAnswerDao,
    private val stressResultDao: StressResultDao,
    private val mentalHealthRepository: MentalHealthRepository
) : ViewModel() {

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex

    private val _totalScore = MutableStateFlow(0)
    val totalScore: StateFlow<Int> = _totalScore

    private val _isTestComplete = MutableStateFlow(false)
    val isTestComplete: StateFlow<Boolean> = _isTestComplete

    // Store scores per question
    private val scores = IntArray(10)

    suspend fun saveAnswer(questionId: Int, score: Int, question: String, answerText: String) {
        val today = currentDate()

        // Clear previous answers for today if first question
        if (questionId == 1) {
            stressAnswerDao.deleteAnswersByDate(today)
        }

        val reversed = if (questionId in listOf(4, 5, 7, 8)) 4 - score else score
        scores[questionId - 1] = reversed

        val entity = StressAnswerEntity(
            questionId = questionId,
            question = question,
            answer = answerText,
            score = reversed,
            date = today
        )
        stressAnswerDao.insertAnswer(entity)
    }

    fun moveToNextQuestion() {
        if (_currentQuestionIndex.value < 9) {
            _currentQuestionIndex.value += 1
        } else {
            // Completed, compute total
            val finalScore = scores.sum()
            _isTestComplete.value = true
            _totalScore.value = finalScore

            // store result
            viewModelScope.launch {
                val answersEncoded = scores.mapIndexed { idx, sc -> "${idx+1}=$sc" }.joinToString(";")
                val interpretation = when {
                    finalScore <= 13 -> "Low stress"
                    finalScore <= 26 -> "Moderate stress"
                    else -> "High perceived stress"
                }
                val recommendation = "Consider stress management strategies and professional help if needed."

                stressResultDao.insertResult(
                    StressResultEntity(
                        answersEncoded = answersEncoded,
                        interpretation = interpretation,
                        recommendation = recommendation
                    )
                )

                // Save to Firebase
                try {
                    val answersList = stressAnswerDao.getAnswersForDate(currentDate()).first()
                    val detailed = answersList.associate { a ->
                        a.questionId.toString() to mapOf(
                            "option" to a.answer,
                            "score" to a.score
                        )
                    }
                    mentalHealthRepository.saveAssessmentAnswers("pss10", detailed, finalScore)
                    val scaled = (finalScore / 40f) * 10f
                    mentalHealthRepository.saveMentalHealthMetric(
                        MentalHealthMetric(stress = scaled)
                    )
                } catch (_: Exception) { }
            }
        }
    }

    fun resetTest() {
        viewModelScope.launch {
            stressAnswerDao.deleteAnswersByDate(currentDate())
        }
        _currentQuestionIndex.value = 0
        _isTestComplete.value = false
        scores.fill(0)
        _totalScore.value = 0
    }

    private fun currentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
} 