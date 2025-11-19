package com.helpyourself.com.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.navigation.NavController
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset

@Composable
fun BreathingExerciseScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var remainingTime by remember { mutableStateOf(30) }
    val infiniteTransition = rememberInfiniteTransition()
    val density = LocalDensity.current
    
    // Animation for the breathing circle
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    // Animation for text opacity
    val textAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000),
            repeatMode = RepeatMode.Reverse
        )
    )    // Timer effect
    LaunchedEffect(Unit) {
        while (remainingTime > 0) {
            delay(1000)
            remainingTime--
        }
        // Navigate back when timer reaches 0
        navController.popBackStack()
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // Multiple animated circles
        (0..7).forEach { index ->
            val rotation by infiniteTransition.animateFloat(
                initialValue = index * 45f,
                targetValue = index * 45f + 180f,
                animationSpec = infiniteRepeatable(
                    animation = tween(4000),
                    repeatMode = RepeatMode.Reverse
                )
            )
            
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(scale)
                    .alpha(0.1f)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .offset { 
                        with(density) {
                            IntOffset(
                                x = ((scale - 1f) * 20.dp.toPx()).toInt(),
                                y = ((scale - 1f) * 20.dp.toPx()).toInt()
                            )
                        }
                    }
            )
        }
        
        // Main breathing circle
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(scale)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
        )

        // Breathing text
        Text(
            text = if (textAlpha > 0.5f) "Inhale" else "Exhale",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.alpha(if (textAlpha > 0.5f) textAlpha else 1f - textAlpha)
        )

        // Timer
        Text(
            text = "$remainingTime",
            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 32.dp),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
