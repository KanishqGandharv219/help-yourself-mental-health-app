package com.helpyourself.com.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import com.helpyourself.com.ui.viewmodels.StressTestViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun StressTestScreen(
    onNavigateBack: () -> Unit,
    viewModel: StressTestViewModel = hiltViewModel()
) {
    val questions = listOf(
        "In the last month, how often have you been upset because of something that happened unexpectedly?",
        "In the last month, how often have you felt that you were unable to control the important things in your life?",
        "In the last month, how often have you felt nervous and stressed?",
        "In the last month, how often have you felt confident about your ability to handle your personal problems?",
        "In the last month, how often have you felt that things were going your way?",
        "In the last month, how often have you found that you could not cope with all the things that you had to do?",
        "In the last month, how often have you been able to control irritations in your life?",
        "In the last month, how often have you felt that you were on top of things?",
        "In the last month, how often have you been angered because of things that happened that were outside of your control?",
        "In the last month, how often have you felt difficulties were piling up so high that you could not overcome them?"
    )

    val options = listOf(
        "Never" to 0,
        "Almost never" to 1,
        "Sometimes" to 2,
        "Fairly often" to 3,
        "Very often" to 4
    )

    val currentQuestionIndex by viewModel.currentQuestionIndex.collectAsState()
    val totalScore by viewModel.totalScore.collectAsState()
    val isTestComplete by viewModel.isTestComplete.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perceived Stress Scale (PSS)") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isTestComplete) {
                Text(
                    text = "Question ${currentQuestionIndex + 1} of ${questions.size}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                AnimatedContent(
                    targetState = currentQuestionIndex,
                    transitionSpec = {
                        (fadeIn() + slideInVertically()) with
                        (fadeOut() + slideOutVertically())
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) { index ->
                    SwipeableQuestionCard(
                        questionText = questions[index],
                        options = options.map { it.first },
                        cardIndex = index,
                        onAnswerSelected = { selectedOption ->
                            scope.launch {
                                val score = options.first { it.first == selectedOption }.second
                                viewModel.saveAnswer(questionId = index + 1, score = score, question = questions[index], answerText = selectedOption)
                                viewModel.moveToNextQuestion()
                            }
                        }
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Assessment Complete",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Your Score: $totalScore/40",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = when {
                            totalScore <= 13 -> "Interpretation: Low stress"
                            totalScore <= 26 -> "Interpretation: Moderate stress"
                            else -> "Interpretation: High perceived stress"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(onClick = { viewModel.resetTest() }) { Text("Start Over") }
                }
            }
        }
    }
}

@Composable
private fun SwipeableQuestionCard(
    questionText: String,
    options: List<String>,
    cardIndex: Int,
    cardOffset: Dp = 0.dp,
    cardScale: Float = 1f,
    onAnswerSelected: (String) -> Unit
) {
    var isAnimatingOut by remember { mutableStateOf(false) }
    val offsetX = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    val gradients = listOf(
        listOf(Color(0xFFE6F3FF), Color(0xFFCCE4FF)),
        listOf(Color(0xFFF5E6FF), Color(0xFFE6CCFF)),
        listOf(Color(0xFFFFE6E6), Color(0xFFFFCCCC)),
        listOf(Color(0xFFE6FFE6), Color(0xFFCCFFCC)),
        listOf(Color(0xFFFFF5E6), Color(0xFFFFE6CC))
    )

    val cardGradient = gradients[cardIndex % gradients.size]
    val textColor = Color(0xFF2C3E50)

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
                text = questionText,
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
                options.forEach { option ->
                    Button(
                        onClick = {
                            if (!isAnimatingOut) {
                                isAnimatingOut = true
                                scope.launch {
                                    val direction = if (options.indexOf(option) < options.size / 2) -1 else 1
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
                        Text(option, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
} 