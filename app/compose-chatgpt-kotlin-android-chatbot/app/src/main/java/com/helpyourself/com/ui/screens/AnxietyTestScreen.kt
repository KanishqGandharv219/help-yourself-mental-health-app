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
import com.helpyourself.com.ui.viewmodels.AnxietyTestViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AnxietyTestScreen(
    viewModel: AnxietyTestViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val questions = listOf(
        "Feeling nervous, anxious or on edge?",
        "Not being able to stop or control worrying?",
        "Worrying too much about different things?",
        "Trouble relaxing?",
        "Being so restless that it is hard to sit still?",
        "Becoming easily annoyed or irritable?",
        "Feeling afraid as if something awful might happen?"
    )

    val options = listOf(
        "Not at all" to 0,
        "Several days" to 1,
        "More than half the days" to 2,
        "Nearly every day" to 3
    )

    val currentQuestionIndex by viewModel.currentQuestionIndex.collectAsState()
    val totalScore by viewModel.totalScore.collectAsState()
    val isTestComplete by viewModel.isTestComplete.collectAsState()
    val scope = rememberCoroutineScope()

    // Cleanup partial answers if user leaves before completing
    DisposableEffect(isTestComplete) {
        onDispose {
            if (!isTestComplete) {
                scope.launch {
                    viewModel.clearIncompleteAnswers()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Anxiety Assessment (GAD-7)") },
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
                                viewModel.saveAnswer(
                                    questionId = index + 1,
                                    question = questions[index],
                                    answer = selectedOption,
                                    score = score
                                )
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
                        text = "Your Score: $totalScore/21",
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = when {
                            totalScore < 5 -> "Interpretation: No anxiety"
                            totalScore < 10 -> "Interpretation: Mild anxiety"
                            totalScore < 15 -> "Interpretation: Moderate anxiety"
                            else -> "Interpretation: Severe anxiety"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.resetTest()
                            }
                        }
                    ) {
                        Text("Start Over")
                    }
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

    // Pastel gradient list similar to depression screen
    val gradients = listOf(
        listOf(Color(0xFFE6F3FF), Color(0xFFCCE4FF)), // Soft Blue
        listOf(Color(0xFFF5E6FF), Color(0xFFE6CCFF)), // Soft Purple
        listOf(Color(0xFFFFE6E6), Color(0xFFFFCCCC)), // Soft Pink
        listOf(Color(0xFFE6FFE6), Color(0xFFCCFFCC)), // Soft Green
        listOf(Color(0xFFFFF5E6), Color(0xFFFFE6CC))  // Soft Orange
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
                                    // Direction: left for first half options, right for second half
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