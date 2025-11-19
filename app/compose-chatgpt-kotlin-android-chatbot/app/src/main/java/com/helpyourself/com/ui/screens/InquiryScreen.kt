package com.helpyourself.com.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helpyourself.com.data.repository.InquiryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.navigation.NavController
import com.helpyourself.com.ui.common.AppRoute

// Data class for assessment questions
data class AssessmentQuestion(
    val id: Int,
    val question: String,
    val options: List<String> = listOf("Never", "Sometimes", "Often", "Always")
)

// UI state for the screen
sealed class InquiryUiState {
    object Initial : InquiryUiState()
    object Loading : InquiryUiState()
    data class Assessment(
        val currentQuestionIndex: Int,
        val questions: List<AssessmentQuestion>,
        val answers: Map<Int, Int>
    ) : InquiryUiState()
    data class Result(val recommendation: String) : InquiryUiState()
    data class Error(val message: String) : InquiryUiState()
}

data class AssessmentCard(
    val id: String,
    val title: String,
    val description: String,
    val questions: List<AssessmentQuestion>,
    val type: String
)

@HiltViewModel
class InquiryViewModel @Inject constructor(
    private val inquiryRepository: InquiryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<InquiryUiState>(InquiryUiState.Initial)
    val uiState: StateFlow<InquiryUiState> = _uiState

    private val questions = listOf(
        AssessmentQuestion(1, "How often do you feel overwhelmed by your daily responsibilities?"),
        AssessmentQuestion(2, "How frequently do you experience difficulty sleeping?"),
        AssessmentQuestion(3, "How often do you feel disconnected from others?"),
        AssessmentQuestion(4, "How frequently do you have trouble concentrating?"),
        AssessmentQuestion(5, "How often do you experience sudden mood changes?")
    )

    init {
        startAssessment()
    }

    private fun startAssessment() {
        _uiState.value = InquiryUiState.Assessment(
            currentQuestionIndex = 0,
            questions = questions,
            answers = emptyMap()
        )
    }

    fun answerQuestion(questionId: Int, answerIndex: Int) {
        val currentState = _uiState.value as? InquiryUiState.Assessment ?: return
        val newAnswers = currentState.answers + (questionId to answerIndex)
        
        if (currentState.currentQuestionIndex < questions.size - 1) {
            // Move to next question
            _uiState.value = currentState.copy(
                currentQuestionIndex = currentState.currentQuestionIndex + 1,
                answers = newAnswers
            )
        } else {
            // All questions answered, calculate result
            calculateResult(newAnswers)
        }
    }

    private fun calculateResult(answers: Map<Int, Int>) {
        viewModelScope.launch {
            _uiState.value = InquiryUiState.Loading
            try {
                // Here you would normally analyze the answers and get a recommendation
                // For now, we'll use a simple scoring system
                val averageScore = answers.values.average()
                val recommendation = when {
                    averageScore <= 1 -> "Your responses suggest low levels of distress. However, it's always good to maintain mental wellness through regular self-care practices."
                    averageScore <= 2 -> "Your responses indicate mild levels of distress. Consider incorporating stress management techniques into your daily routine."
                    averageScore <= 3 -> "Your responses suggest moderate levels of distress. It might be helpful to speak with a mental health professional."
                    else -> "Your responses indicate significant levels of distress. We strongly recommend consulting with a mental health professional."
                }
                _uiState.value = InquiryUiState.Result(recommendation)
            } catch (e: Exception) {
                _uiState.value = InquiryUiState.Error("Failed to process assessment: ${e.message}")
            }
        }
    }

    fun restart() {
        startAssessment()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InquiryScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: InquiryViewModel = hiltViewModel()
) {
    val assessmentCards = listOf(
        AssessmentCard(
            id = "general_inquiry",
            title = "Mental Health Inquiry",
            description = "Have questions about your mental health? This interactive consultation helps you express your concerns and get guidance. Share your thoughts and feelings in a structured way to better understand your mental well-being.",
            type = "Consultation",
            questions = listOf(
                AssessmentQuestion(1, "How often do you feel overwhelmed by your daily responsibilities?"),
                AssessmentQuestion(2, "How frequently do you experience difficulty sleeping?"),
                AssessmentQuestion(3, "How often do you feel disconnected from others?"),
                AssessmentQuestion(4, "How frequently do you have trouble concentrating?"),
                AssessmentQuestion(5, "How often do you experience sudden mood changes?")
            )
        ),
        AssessmentCard(
            id = "depression_who",
            title = "Depression Test (WHO-5)",
            description = "The World Health Organization's Five Well-Being Index (WHO-5) is a validated screening tool for depression. This quick assessment helps evaluate your emotional well-being over the past two weeks.",
            type = "Depression Screening",
            questions = listOf(
                AssessmentQuestion(1, "I have felt cheerful and in good spirits"),
                AssessmentQuestion(2, "I have felt calm and relaxed"),
                AssessmentQuestion(3, "I have felt active and vigorous"),
                AssessmentQuestion(4, "I woke up feeling fresh and rested"),
                AssessmentQuestion(5, "My daily life has been filled with things that interest me")
            )
        ),
        AssessmentCard(
            id = "anxiety_gad7",
            title = "Anxiety Assessment (GAD-7)",
            description = "The Generalized Anxiety Disorder-7 (GAD-7) is a widely used screening tool for anxiety. This assessment helps identify potential anxiety symptoms and their impact on daily life.",
            type = "Anxiety Screening",
            questions = listOf(
                AssessmentQuestion(1, "Feeling nervous, anxious, or on edge"),
                AssessmentQuestion(2, "Not being able to stop or control worrying"),
                AssessmentQuestion(3, "Worrying too much about different things"),
                AssessmentQuestion(4, "Trouble relaxing"),
                AssessmentQuestion(5, "Being so restless that it's hard to sit still")
            )
        ),
        AssessmentCard(
            id = "stress_pss",
            title = "Perceived Stress Scale",
            description = "The Perceived Stress Scale (PSS) is a classic stress assessment instrument. This tool helps you understand how different situations affect your feelings and perceived stress levels.",
            type = "Stress Assessment",
            questions = listOf(
                AssessmentQuestion(1, "How often have you been upset because of something that happened unexpectedly?"),
                AssessmentQuestion(2, "How often have you felt that you were unable to control the important things in your life?"),
                AssessmentQuestion(3, "How often have you felt nervous and stressed?"),
                AssessmentQuestion(4, "How often have you felt confident about your ability to handle your personal problems?"),
                AssessmentQuestion(5, "How often have you felt that things were going your way?")
            )
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mental Health Assessments") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(assessmentCards) { card ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Type label
                        Text(
                            text = card.type,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Title
                        Text(
                            text = card.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Description
                        Text(
                            text = card.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Action button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalButton(
                                onClick = { 
                                    if (card.id == "general_inquiry") {
                                        navController.navigate(AppRoute.InquiryDetails.route)
                                    } else if (card.id == "depression_who") {
                                        navController.navigate(AppRoute.DepressionTest.route)
                                    } else if (card.id == "anxiety_gad7") {
                                        navController.navigate(AppRoute.AnxietyTest.route)
                                    } else if (card.id == "stress_pss") {
                                        navController.navigate(AppRoute.StressTest.route)
                                    } else {
                                        // Handle other assessment starts
                                    }
                                },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Text(if (card.id == "general_inquiry") "Ask" else "Start Assessment")
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = if (card.id == "general_inquiry") "Ask" else "Start",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
} 