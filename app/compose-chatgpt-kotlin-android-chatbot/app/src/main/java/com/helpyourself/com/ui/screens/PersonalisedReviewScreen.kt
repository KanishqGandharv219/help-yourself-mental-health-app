package com.helpyourself.com.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.helpyourself.com.data.local.database.AppDatabase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.flow.first
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.flow.firstOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import com.helpyourself.com.BuildConfig
import kotlinx.coroutines.withContext
import org.json.JSONArray
import kotlinx.coroutines.Dispatchers
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign

@Composable
fun PersonalisedReviewScreen(navController: NavHostController) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val scrollState = rememberScrollState()

    var reviewText by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Personalised Mental Health Review",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Show review or button
                if (reviewText == null) {
                    ElevatedButton(
                        onClick = {
                            coroutineScope.launch {
                                isLoading = true
                                reviewText = generateReview(db)
                                isLoading = false
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = colorScheme.primaryContainer,
                            contentColor = colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text(
                            "Generate Review",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (isLoading) {
                    Spacer(modifier = Modifier.height(32.dp))
                    CircularProgressIndicator(
                        color = colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }

                AnimatedVisibility(
                    visible = reviewText != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    reviewText?.let { text ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                                .shadow(
                                    elevation = 12.dp,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clip(RoundedCornerShape(20.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                colorScheme.primaryContainer.copy(alpha = 0.7f),
                                                colorScheme.secondaryContainer.copy(alpha = 0.7f)
                                            )
                                        )
                                    )
                                    .padding(24.dp)
                            ) {
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = colorScheme.onSurface,
                                    lineHeight = 24.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                
                // Add extra space at the bottom for the floating button
                Spacer(modifier = Modifier.height(80.dp))
            }

            // Floating button at the bottom
            AnimatedVisibility(
                visible = reviewText != null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                ElevatedButton(
                    onClick = { reviewText = null },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = colorScheme.primaryContainer,
                        contentColor = colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(
                        "Generate New Review",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private suspend fun generateReview(db: AppDatabase): String {
    return try {
        // Collect last scores (today)
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val depressionAnswers = db.depressionAnswerDao().getAnswersForDate(date).first()
        val anxietyAnswers = db.anxietyAnswerDao().getAnswersForDate(date).first()
        val stressAnswers = db.stressAnswerDao().getAnswersForDate(date).first()

        val depressionSection = if (depressionAnswers.isNotEmpty()) {
            depressionAnswers.joinToString(separator = "\n") { "• ${it.question}: ${it.answer}" }
        } else "No depression questions answered today."

        val anxietySection = if (anxietyAnswers.isNotEmpty()) {
            anxietyAnswers.joinToString(separator = "\n") { "• ${it.question}: ${it.answer} (score ${it.score})" }
        } else "No anxiety questions answered today."

        val stressSection = if (stressAnswers.isNotEmpty()) {
            stressAnswers.joinToString(separator = "\n") { "• ${it.question}: ${it.answer} (score ${it.score})" }
        } else "No stress questions answered today."

        val prompt = """
            Today's mental-health questionnaire results:

            Depression Responses:
            $depressionSection

            Anxiety Responses:
            $anxietySection

            Stress Responses:
            $stressSection

            Based on these responses, provide a personalised yet concise review (≤ 180 words) that explains possible interpretations, flags any concerning patterns, and suggests gentle, actionable recommendations. Conclude with an encouraging sentence.""".trimIndent()

        // Call Gemini-pro model via REST
        Log.d("PersonalisedReview", "Prompt:\n$prompt")

        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) {
            return "Gemini API key not configured."
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey"
        val jsonBody = run {
            val partsArr = JSONArray().put(JSONObject().put("text", prompt))
            val contentArr = JSONArray().put(JSONObject().put("parts", partsArr))
            JSONObject().put("contents", contentArr).toString()
        }

        val client = OkHttpClient()
        val requestBody = jsonBody.toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url(url).post(requestBody).build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) {
                    val errBody = resp.body?.string()
                    Log.e("PersonalisedReview", "Gemini API error: ${resp.code} - ${errBody ?: "no body"}")
                    return@withContext "Failed to fetch review: ${errBody ?: "HTTP ${resp.code}"}"
                }
                val bodyStr = resp.body?.string() ?: return@withContext "Empty response from Gemini API"
                val json = JSONObject(bodyStr)
                val text = json
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                text.trim()
            }
        }
    } catch (e: Exception) {
        Log.e("PersonalisedReview", "Error generating review", e)
        "Could not generate review: ${e.message}"
    }
} 