package com.helpyourself.com.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import com.helpyourself.com.data.local.database.AppDatabase
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentalHealthAnalysisScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    
    Surface(
        modifier = modifier.fillMaxSize(),
        color = colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Mental Health Analysis",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Trend Graphs
            TrendGraphsSection()
            
            Spacer(modifier = Modifier.height(32.dp))

            // Combined Graph instead of comparison blocks
            CombinedMetricsGraph()

            Spacer(modifier = Modifier.height(24.dp))

            // Button to personalised review
            Button(
                onClick = {
                    navController.navigate(com.helpyourself.com.ui.common.AppRoute.PersonalisedReview.route)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Get Personalised Review",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun CombinedMetricsGraph() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val calendar = remember { Calendar.getInstance() }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val colorScheme = MaterialTheme.colorScheme
    
    val dates = remember {
        (0 until 7).map { daysAgo ->
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
            dateFormat.format(calendar.time)
        }.reversed()
    }

    // Collect all metrics
    val depressionScores = dates.map { date ->
        val score by db.depressionAnswerDao().getAnswersForDate(date)
            .map { list -> list.count { it.answer.equals("yes", ignoreCase = true) } }
            .collectAsState(initial = 0)
        score
    }

    val anxietyScores = dates.map { date ->
        val score by db.anxietyAnswerDao().getTotalScoreForDate(date)
            .collectAsState(initial = 0)
        score
    }

    val stressScores = dates.map { date ->
        val score by db.stressAnswerDao().getAnswersForDate(date)
            .map { list -> list.sumOf { it.score } }
            .collectAsState(initial = 0)
        score
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Combined Metrics",
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onSurface
                )
                
                // Small color indicators with labels
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        "D" to Color(0xFFB388FF),
                        "A" to Color(0xFF03DAC5),
                        "S" to Color(0xFFFF4081)
                    ).forEach { (label, color) ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp)
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val width = size.width
                    val height = size.height
                    
                    // Normalize all scores to 0-1 range for comparison
                    val maxDepression = depressionScores.maxOrNull()?.toFloat() ?: 1f
                    val maxAnxiety = anxietyScores.maxOrNull()?.toFloat() ?: 1f
                    val maxStress = stressScores.maxOrNull()?.toFloat() ?: 1f
                    
                    // Draw lines for each metric
                    val metrics = listOf(
                        Triple(depressionScores, maxDepression, Color(0xFFB388FF)),
                        Triple(anxietyScores, maxAnxiety, Color(0xFF03DAC5)),
                        Triple(stressScores, maxStress, Color(0xFFFF4081))
                    )
                    
                    metrics.forEach { (scores, maxScore, color) ->
                        val path = Path()
                        scores.forEachIndexed { index, score ->
                            val x = (index.toFloat() / (scores.size - 1)) * width
                            val y = height - (score.toFloat() / maxScore) * height
                            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        
                        drawPath(
                            path = path,
                            color = color,
                            style = Stroke(
                                width = 2.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                        
                        // Draw smaller points
                        scores.forEachIndexed { index, score ->
                            val x = (index.toFloat() / (scores.size - 1)) * width
                            val y = height - (score.toFloat() / maxScore) * height
                            
                            drawCircle(
                                color = color,
                                radius = 2.dp.toPx(),
                                center = Offset(x, y)
                            )
                        }
                    }
                }
                
                // Date labels
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = SimpleDateFormat("MM/dd", Locale.getDefault())
                            .format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            .parse(dates.first())!!),
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurface.copy(alpha = 0.7f)
                    )
        Text(
                        text = "Today",
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurface.copy(alpha = 0.7f)
        )
                }
            }
        }
    }
}

@Composable
private fun TrendGraphsSection() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val calendar = remember { Calendar.getInstance() }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val colorScheme = MaterialTheme.colorScheme
    
    val dates = remember {
        (0 until 7).map { daysAgo ->
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
            dateFormat.format(calendar.time)
        }.reversed()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Depression Graph - Using lighter purple
        TrendGraph(
            title = "Depression Symptoms",
            color = Color(0xFFB388FF),
            gradientColors = listOf(
                Color(0xFFB388FF).copy(alpha = 0.5f),
                Color(0xFFB388FF).copy(alpha = 0.1f)
            ),
            dates = dates,
            db = db
        ) { date ->
            db.depressionAnswerDao().getAnswersForDate(date).map { list ->
                list.count { it.answer.equals("yes", ignoreCase = true) }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Anxiety Graph
        TrendGraph(
            title = "Anxiety Level",
            color = Color(0xFF03DAC5),
            gradientColors = listOf(
                Color(0xFF03DAC5).copy(alpha = 0.5f),
                Color(0xFF03DAC5).copy(alpha = 0.1f)
            ),
            dates = dates,
            db = db
        ) { date ->
            db.anxietyAnswerDao().getTotalScoreForDate(date)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Stress Graph
        TrendGraph(
            title = "Stress Level",
            color = Color(0xFFFF4081),
            gradientColors = listOf(
                Color(0xFFFF4081).copy(alpha = 0.5f),
                Color(0xFFFF4081).copy(alpha = 0.1f)
            ),
            dates = dates,
            db = db
        ) { date ->
            db.stressAnswerDao().getAnswersForDate(date).map { list ->
                list.sumOf { it.score }
            }
        }
    }
}

@Composable
private fun TrendGraph(
    title: String,
    color: Color,
    gradientColors: List<Color>,
    dates: List<String>,
    db: AppDatabase,
    modifier: Modifier = Modifier,
    getScoreFlow: (String) -> kotlinx.coroutines.flow.Flow<Int>
) {
    val scores = dates.map { date ->
        val score by getScoreFlow(date).collectAsState(initial = 0)
        date to score
    }
    
    val colorScheme = MaterialTheme.colorScheme
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = color,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
                Box(
                    modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val width = size.width
                    val height = size.height
                    val maxScore = scores.maxOf { it.second }.coerceAtLeast(1)
                    
                    // Draw gradient background
                    val path = Path()
                    path.moveTo(0f, height)
                    scores.forEachIndexed { index, (_, score) ->
                        val x = (index.toFloat() / (scores.size - 1)) * width
                        val y = height - (score.toFloat() / maxScore) * height
                        if (index == 0) path.lineTo(x, y) else path.lineTo(x, y)
                    }
                    path.lineTo(width, height)
                    path.close()
                    
                    drawPath(
                        path = path,
                        brush = Brush.verticalGradient(gradientColors)
                    )
                    
                    // Draw line
                    val linePath = Path()
                    scores.forEachIndexed { index, (_, score) ->
                        val x = (index.toFloat() / (scores.size - 1)) * width
                        val y = height - (score.toFloat() / maxScore) * height
                        if (index == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
                    }
                    
                    drawPath(
                        path = linePath,
                        color = color,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                    
                    // Draw points
                    scores.forEachIndexed { index, (_, score) ->
                        val x = (index.toFloat() / (scores.size - 1)) * width
                        val y = height - (score.toFloat() / maxScore) * height
                        
                        // Draw outer circle
                        drawCircle(
                            color = colorScheme.surface,
                            radius = 5.dp.toPx(),
                            center = Offset(x, y)
                        )
                        
                        // Draw inner circle
                        drawCircle(
                            color = color,
                            radius = 3.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                }
                
                // Add date labels
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = SimpleDateFormat("MM/dd", Locale.getDefault())
                            .format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            .parse(dates.first())!!),
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Today",
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}