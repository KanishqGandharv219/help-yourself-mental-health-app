package com.helpyourself.com.ui.common

sealed class AppRoute(val route: String) {
    object Conversation : AppRoute("conversation")
    object Settings : AppRoute("settings")
    object AcademicResources : AppRoute("academic_resources")
    object MentalHealthResources : AppRoute("mental_health_resources")
    object MentalHealthAnalysis : AppRoute("mental_health_analysis")
    object BreathingExercise : AppRoute("breathing_exercise")
    object About : AppRoute("about")
    object TherapyNearMe : AppRoute("therapy_near_me")
    object Inquiry : AppRoute("inquiry")
    object InquiryDetails : AppRoute("inquiry_details")
    object DepressionTest : AppRoute("depression_test")
    object AnxietyTest : AppRoute("anxiety_test")
    object StressTest : AppRoute("stress_test")
    object PersonalisedReview : AppRoute("personalised_review")
    object DatabaseTest : AppRoute("database_test")
    
    companion object {
        fun fromRoute(route: String?): AppRoute {            
            return when (route) {
                Conversation.route -> Conversation
                Settings.route -> Settings
                AcademicResources.route -> AcademicResources
                MentalHealthResources.route -> MentalHealthResources
                MentalHealthAnalysis.route -> MentalHealthAnalysis
                BreathingExercise.route -> BreathingExercise
                About.route -> About
                TherapyNearMe.route -> TherapyNearMe
                Inquiry.route -> Inquiry
                InquiryDetails.route -> InquiryDetails
                DepressionTest.route -> DepressionTest
                AnxietyTest.route -> AnxietyTest
                StressTest.route -> StressTest
                PersonalisedReview.route -> PersonalisedReview
                DatabaseTest.route -> DatabaseTest
                else -> Conversation
            }
        }
    }
}