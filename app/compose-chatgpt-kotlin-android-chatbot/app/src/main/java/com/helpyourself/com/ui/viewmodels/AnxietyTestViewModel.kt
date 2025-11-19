package com.helpyourself.com.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helpyourself.com.data.local.dao.AnxietyAnswerDao
import com.helpyourself.com.data.local.entities.AnxietyAnswerEntity
import com.helpyourself.com.data.local.dao.AnxietyResultDao
import com.helpyourself.com.data.local.entities.AnxietyResultEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.first
import com.helpyourself.com.data.repository.MentalHealthRepository
import com.helpyourself.com.data.model.MentalHealthMetric

@HiltViewModel
class AnxietyTestViewModel @Inject constructor(
    private val anxietyAnswerDao: AnxietyAnswerDao,
    private val anxietyResultDao: AnxietyResultDao,
    private val mentalHealthRepository: MentalHealthRepository
) : ViewModel() {

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex

    private val _totalScore = MutableStateFlow(0)
    val totalScore: StateFlow<Int> = _totalScore

    private val _isTestComplete = MutableStateFlow(false)
    val isTestComplete: StateFlow<Boolean> = _isTestComplete

    init {
        val today = currentDate()
        viewModelScope.launch {
            anxietyAnswerDao.getTotalScoreForDate(today).collect { score ->
                _totalScore.value = score ?: 0
            }
        }
    }

    suspend fun saveAnswer(questionId: Int, question: String, answer: String, score: Int) {
        val today = currentDate()

        // If this is the first question, clear previous answers for today
        if (questionId == 1) {
            anxietyAnswerDao.deleteAnswersByDate(today)
        }

        val answerEntity = AnxietyAnswerEntity(
            questionId = questionId,
            question = question,
            answer = answer,
            score = score,
            date = today
        )
        anxietyAnswerDao.insertAnswer(answerEntity)
    }

    fun moveToNextQuestion() {
        if (_currentQuestionIndex.value < 6) {
            _currentQuestionIndex.value += 1
        } else {
            _isTestComplete.value = true
            // Store result entity
            val interpretation = when {
                _totalScore.value < 5 -> "No anxiety"
                _totalScore.value < 10 -> "Mild anxiety"
                _totalScore.value < 15 -> "Moderate anxiety"
                else -> "Severe anxiety"
            }

            viewModelScope.launch {
                val list = anxietyAnswerDao.getAnswersForDate(currentDate()).first()
                val encoded = list.joinToString(";") { "${it.questionId}=${it.score}" }
                anxietyResultDao.insertResult(
                    AnxietyResultEntity(
                        answersEncoded = encoded,
                        interpretation = interpretation,
                        recommendation = "Consider consulting a mental health professional if needed."
                    )
                )

                // Save to Firebase
                try {
                    val detailed = list.associate { entry ->
                        entry.questionId.toString() to mapOf(
                            "option" to entry.answer,
                            "score" to entry.score
                        )
                    }
                    mentalHealthRepository.saveAssessmentAnswers("gad7", detailed, _totalScore.value)
                    val scaled = (_totalScore.value / 21f) * 10f
                    mentalHealthRepository.saveMentalHealthMetric(
                        MentalHealthMetric(anxiety = scaled)
                    )
                } catch (_: Exception) { }
            }
        }
    }

    suspend fun resetTest() {
        val today = currentDate()
        anxietyAnswerDao.deleteAnswersByDate(today)
        _currentQuestionIndex.value = 0
        _isTestComplete.value = false
        _totalScore.value = 0
    }

    suspend fun clearIncompleteAnswers() {
        val today = currentDate()
        anxietyAnswerDao.deleteAnswersByDate(today)
    }

    private fun currentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
} 