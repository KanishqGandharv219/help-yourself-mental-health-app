package com.helpyourself.com.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs
import com.helpyourself.com.data.local.dao.DepressionResultDao
import com.helpyourself.com.data.local.entities.DepressionResultEntity
import com.helpyourself.com.data.local.dao.DepressionAnswerDao
import com.helpyourself.com.data.local.entities.DepressionAnswerEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.helpyourself.com.data.repository.MentalHealthRepository
import com.helpyourself.com.data.model.MentalHealthMetric

data class DepressionQuestion(
    val id: Int,
    val question: String,
    val options: List<String> = listOf("Yes", "No", "Don't know", "Refuse"),
    val dependsOn: Map<Int, String>? = null // Map of question ID to required answer for this question to be shown
)

sealed class DepressionTestState {
    object Initial : DepressionTestState()
    data class InProgress(
        val currentQuestionId: Int,
        val answers: Map<Int, String>
    ) : DepressionTestState()
    data class Complete(
        val interpretation: String,
        val recommendation: String
    ) : DepressionTestState()
}

@HiltViewModel
class DepressionTestViewModel @Inject constructor(
    private val depressionResultDao: DepressionResultDao,
    private val depressionAnswerDao: DepressionAnswerDao,
    private val mentalHealthRepository: MentalHealthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<DepressionTestState>(DepressionTestState.Initial)
    val uiState: StateFlow<DepressionTestState> = _uiState

    private val questions = mapOf(
        1 to DepressionQuestion(1, "Have you ever been told by a doctor or other health worker that you have depression?"),
        5 to DepressionQuestion(5, "Have you been taking any medications or other treatment, like counseling or psychotherapy, either alone or in group, for depression in the last 2 weeks?"),
        6 to DepressionQuestion(6, "During the last 12 months, have you had a period lasting several days when you felt sad, empty or depressed?"),
        7 to DepressionQuestion(7, "During the last 12 months, have you had a period lasting several days when you lost interest in most things you usually enjoy such as personal relationships, work or hobbies/recreation?"),
        8 to DepressionQuestion(8, "During the last 12 months, have you had a period lasting several days when you have been feeling your energy decreased or that you are tired all the time?"),
        9 to DepressionQuestion(9, "Was this period of sadness, loss of interest or low energy for more than 2 weeks?"),
        10 to DepressionQuestion(10, "Was this period of sadness, loss of interest or low energy most of the day, nearly every day?"),
        11 to DepressionQuestion(11, "Did you lose your appetite?"),
        12 to DepressionQuestion(12, "Did you notice any slowing down in your thinking?"),
        13 to DepressionQuestion(13, "Did you notice any problems falling asleep?"),
        14 to DepressionQuestion(14, "Did you notice any problems waking up too early?"),
        15 to DepressionQuestion(15, "Did you have any difficulties concentrating; for example, listening to others, working, watching TV, listening to the radio?"),
        16 to DepressionQuestion(16, "Did you notice any slowing down in your moving around?"),
        17 to DepressionQuestion(17, "Did you feel anxious and worried most days?"),
        18 to DepressionQuestion(18, "Were you so restless or jittery nearly every day that you paced up and down and couldn't sit still?"),
        19 to DepressionQuestion(19, "Did you feel negative about yourself or like you had lost confidence?"),
        20 to DepressionQuestion(20, "Did you frequently feel hopeless - that there was no way to improve things?"),
        21 to DepressionQuestion(21, "Did your interest in sex decrease?"),
        22 to DepressionQuestion(22, "Did you think of death, or wish you were dead?"),
        23 to DepressionQuestion(23, "During this period, did you ever try to end your life?")
    )

    init {
        startTest()
    }

    private fun startTest() {
        // Clear any previous answers for today to ensure only latest results are stored
        viewModelScope.launch {
            val today = currentDate()
            depressionAnswerDao.deleteAnswersByDate(today)
        }
        _uiState.value = DepressionTestState.InProgress(
            currentQuestionId = 1,
            answers = emptyMap()
        )
    }

    fun answerQuestion(answer: String) {
        val currentState = _uiState.value as? DepressionTestState.InProgress ?: return
        val newAnswers = currentState.answers + (currentState.currentQuestionId to answer)
        
        val nextQuestionId = getNextQuestionId(currentState.currentQuestionId, newAnswers)
        
        if (nextQuestionId != null) {
            _uiState.value = currentState.copy(
                currentQuestionId = nextQuestionId,
                answers = newAnswers
            )
        } else {
            calculateResult(newAnswers)
        }
    }

    private fun getNextQuestionId(currentId: Int, answers: Map<Int, String>): Int? {
        return when (currentId) {
            1 -> 5  // After question 1, always go to 5
            5 -> 6  // After question 5, always go to 6
            6 -> 7  // After question 6, always go to 7
            7 -> 8  // After question 7, always go to 8
            8 -> {
                // After question 8, check if any of 6,7,8 were "Yes"
                if (answers[6] == "Yes" || answers[7] == "Yes" || answers[8] == "Yes") {
                    9  // If yes, continue to detailed questions
                } else {
                    null  // If no, end test
                }
            }
            9 -> {
                if (answers[9] == "Yes") 10 else null
            }
            10 -> {
                if (answers[10] == "Yes") 11 else null
            }
            in 11..22 -> {
                // For questions 11-22, just go to next question
                currentId + 1
            }
            23 -> null  // End of test
            else -> null
        }
    }

    private fun calculateResult(answers: Map<Int, String>) {
        val interpretation = when {
            // Previously diagnosed depression
            answers[1] == "Yes" -> {
                if (answers[5] == "Yes") {
                    "You have previously been diagnosed with depression and are currently under treatment."
                } else {
                    "You have previously been diagnosed with depression but are not currently under treatment."
                }
            }
            
            // No current symptoms
            answers[6] == "No" && answers[7] == "No" && answers[8] == "No" -> {
                "Based on your responses, you are not currently experiencing significant depressive symptoms."
            }
            
            // Has symptoms but not meeting duration/frequency criteria
            answers[9] == "No" || answers[10] == "No" -> {
                "Your responses indicate some mood symptoms, but they may not meet the duration or frequency criteria for clinical depression."
            }
            
            else -> {
                // Count symptoms from questions 11-23
                val symptomCount = (11..23).count { answers[it] == "Yes" }
                when {
                    symptomCount >= 8 -> "Your responses suggest significant depressive symptoms that may indicate severe depression."
                    symptomCount >= 5 -> "Your responses suggest moderate depressive symptoms."
                    symptomCount >= 3 -> "Your responses suggest mild depressive symptoms."
                    else -> "Your responses indicate some depressive symptoms, but they may not meet clinical criteria."
                }
            }
        }

        val recommendation = when {
            answers[22] == "Yes" || answers[23] == "Yes" -> {
                "IMPORTANT: Your responses indicate thoughts of death or self-harm. Please seek immediate help from a mental health professional or emergency services. You can also contact our emergency support services for immediate assistance."
            }
            answers[1] == "Yes" && answers[5] == "No" -> {
                "Since you have a history of depression but are not currently under treatment, it is recommended to consult with a mental health professional for an evaluation."
            }
            interpretation.contains("severe") -> {
                "It is strongly recommended to consult with a mental health professional for a thorough evaluation and appropriate treatment."
            }
            interpretation.contains("moderate") -> {
                "Consider speaking with a mental health professional to discuss your symptoms and potential treatment options."
            }
            interpretation.contains("mild") -> {
                "Consider implementing self-care strategies and monitoring your symptoms. If they persist or worsen, consult with a mental health professional."
            }
            else -> {
                "Continue monitoring your mental health and practice self-care. If you notice any changes or worsening symptoms, don't hesitate to seek professional support."
            }
        }

        _uiState.value = DepressionTestState.Complete(
            interpretation = interpretation,
            recommendation = recommendation
        )

        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            val today = currentDate()

            // Save individual answers with full question text
            val answerEntities = answers.map { (qId, ans) ->
                DepressionAnswerEntity(
                    questionId = qId,
                    question = questions[qId]?.question ?: "",
                    answer = ans,
                    timestamp = timestamp,
                    date = today
                )
            }
            depressionAnswerDao.insertAnswers(answerEntities)

            // Save summary result
            val answersEncoded = answers.entries.joinToString(";") { "${'$'}{it.key}=${'$'}{it.value}" }
            val entity = DepressionResultEntity(
                answersEncoded = answersEncoded,
                interpretation = interpretation,
                recommendation = recommendation,
                timestamp = timestamp
            )
            depressionResultDao.insertResult(entity)
        }

        // Build detailed answers map for Firebase
        val detailedAnswers: Map<String, Map<String, Any>> = answers.map { (k, v) ->
            val s = if (v == "Yes") 1 else 0
            k.toString() to mapOf("option" to v, "score" to s)
        }.toMap()
        val score = detailedAnswers.values.sumOf { (it["score"] as Int) }

        // Save to Firebase
        viewModelScope.launch {
            try {
                mentalHealthRepository.saveAssessmentAnswers("who_steps", detailedAnswers, score)
                // Map score to 0-10 scale crudely (max 23 yeses)
                val scaled = (score / 23f) * 10f
                mentalHealthRepository.saveMentalHealthMetric(
                    MentalHealthMetric(depression = scaled)
                )
            } catch (_: Exception) {
                // ignore for now
            }
        }
    }

    fun restartTest() {
        startTest()
    }

    fun getCurrentQuestion(): DepressionQuestion? {
        val currentState = _uiState.value as? DepressionTestState.InProgress ?: return null
        return questions[currentState.currentQuestionId]
    }

    fun getQuestionById(id: Int): DepressionQuestion? = questions[id]

    private fun currentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}

@Composable
private fun SwipeableQuestionCard(
    question: DepressionQuestion,
    cardIndex: Int,
    cardOffset: Dp,
    cardScale: Float,
    onAnswerSelected: (String) -> Unit
) {
    key(question.id) { // Ensure state is reset per card
        var isAnimatingOut by remember { mutableStateOf(false) }
        val offsetX = remember { Animatable(0f) }
        val rotation = remember { Animatable(0f) }
        val scale = remember { Animatable(1f) }
        val scope = rememberCoroutineScope()
        val density = LocalDensity.current
        
        // Soft, pastel gradients that are easy on the eyes
        val gradients = listOf(
            listOf(Color(0xFFE6F3FF), Color(0xFFCCE4FF)), // Soft Blue
            listOf(Color(0xFFF5E6FF), Color(0xFFE6CCFF)), // Soft Purple
            listOf(Color(0xFFFFE6E6), Color(0xFFFFCCCC)), // Soft Pink
            listOf(Color(0xFFE6FFE6), Color(0xFFCCFFCC)), // Soft Green
            listOf(Color(0xFFFFF5E6), Color(0xFFFFE6CC))  // Soft Orange
        )
        
        val cardGradient = gradients[cardIndex % gradients.size]
        val textColor = Color(0xFF2C3E50) // Dark blue-gray for good readability

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = cardOffset)
                .graphicsLayer {
                    translationX = offsetX.value
                    rotationZ = rotation.value
                    scaleX = scale.value * cardScale
                    scaleY = scale.value * cardScale
                    cameraDistance = 12f * density.density
                }
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = cardGradient,
                        start = Offset(0f, 0f),
                        end = Offset(400f, 400f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = question.question,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    question.options.forEach { option ->
                        Button(
                            onClick = {
                                if (!isAnimatingOut) {
                                    isAnimatingOut = true
                                    scope.launch {
                                        // Determine direction based on answer
                                        val direction = when (option) {
                                            "Yes" -> 1
                                            "No" -> -1
                                            else -> if (question.options.indexOf(option) < question.options.size / 2) -1 else 1
                                        }
                                        
                                        // Animate out
                                        rotation.animateTo(20f * direction, tween(300, easing = FastOutSlowInEasing))
                                        scale.animateTo(0.8f, tween(300))
                                        offsetX.animateTo(1500f * direction, tween(500, easing = FastOutLinearInEasing))
                                        onAnswerSelected(option)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = option,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DepressionTestScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: DepressionTestViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WHO STEPS Depression Assessment") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is DepressionTestState.Initial -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                is DepressionTestState.InProgress -> {
                    val currentQuestion = viewModel.getCurrentQuestion()
                    if (currentQuestion != null) {
                        AnimatedContent(
                            targetState = currentQuestion.id,
                            transitionSpec = {
                                (fadeIn() + slideInVertically()) with
                                (fadeOut() + slideOutVertically())
                            },
                            modifier = Modifier.align(Alignment.Center)
                        ) { targetId ->
                            val q = viewModel.getQuestionById(targetId) ?: return@AnimatedContent
                            SwipeableQuestionCard(
                                question = q,
                                cardIndex = targetId,
                                cardOffset = 0.dp,
                                cardScale = 1f,
                                onAnswerSelected = { answer -> viewModel.answerQuestion(answer) }
                            )
                        }
                    }
                }
                
                is DepressionTestState.Complete -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Assessment Complete",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                        
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = state.interpretation,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                
                                Text(
                                    text = state.recommendation,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        FilledTonalButton(
                            onClick = { viewModel.restartTest() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Take the Test Again")
                        }
                    }
                }
            }
        }
    }
} 