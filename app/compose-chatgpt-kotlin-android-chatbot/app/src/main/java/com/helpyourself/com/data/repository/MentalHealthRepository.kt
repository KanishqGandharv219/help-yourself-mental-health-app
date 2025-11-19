package com.helpyourself.com.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.helpyourself.com.data.model.MentalHealthMetric
import com.helpyourself.com.data.model.AssessmentQuestion
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class MentalHealthRepository @Inject constructor() {
    private val databaseUrl = "https://sign-in-9650c-default-rtdb.firebaseio.com/"
    private val database = FirebaseDatabase.getInstance(databaseUrl).apply {
        // Enable offline persistence BEFORE any reference is used
        setPersistenceEnabled(true)
    }
    private val metricsRef = database.getReference("mental_health_metrics")
    private val assessmentQuestionsRef = database.getReference("assessment_questions")
    private val assessmentAnswersRef = database.getReference("assessment_answers")
    private val auth = FirebaseAuth.getInstance()

    // Connection test: attempt a lightweight read on user metrics; success => connected and authorized
    suspend fun testDatabaseConnection(): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            metricsRef.child(uid).limitToFirst(1).get().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun saveMentalHealthMetric(metric: MentalHealthMetric) {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User must be logged in")
        val userMetricsRef = metricsRef.child(userId)
        val newMetricRef = userMetricsRef.push()
        newMetricRef.setValue(metric).await()
    }

    suspend fun getMetricsForUser(startTime: Long, endTime: Long): List<MentalHealthMetric> {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User must be logged in")
        return metricsRef.child(userId)
            .orderByChild("timestamp")
            .startAt(startTime.toDouble())
            .endAt(endTime.toDouble())
            .get()
            .await()
            .children
            .mapNotNull { it.getValue<MentalHealthMetric>() }
    }

    suspend fun getLatestMetric(): MentalHealthMetric? {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User must be logged in")
        return metricsRef.child(userId)
            .orderByChild("timestamp")
            .limitToLast(1)
            .get()
            .await()
            .children
            .firstOrNull()
            ?.getValue<MentalHealthMetric>()
    }

    suspend fun deleteMetric(timestamp: Long) {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
        metricsRef.child(userId)
            .child(timestamp.toString())
            .removeValue()
            .await()
    }

    // Fetch all questions for an assessment type (e.g., "gad7", "phq9", "pss")
    suspend fun getAssessmentQuestions(type: String): List<AssessmentQuestion> {
        return assessmentQuestionsRef.child(type)
            .get()
            .await()
            .children
            .mapNotNull { it.getValue<AssessmentQuestion>() }
    }

    // Save user's answers & score
    suspend fun saveAssessmentAnswers(
        type: String,
        answers: Map<String, Any>,
        score: Int
    ) {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User must be logged in")
        val data = mapOf(
            "timestamp" to System.currentTimeMillis(),
            "type" to type,
            "answers" to answers,
            "score" to score
        )
        assessmentAnswersRef.child(userId).child(type).push().setValue(data).await()
    }
} 