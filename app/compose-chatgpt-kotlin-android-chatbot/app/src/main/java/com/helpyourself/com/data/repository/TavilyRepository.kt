package com.helpyourself.com.data.repository

import android.util.Log
import com.helpyourself.com.data.api.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TavilyRepository @Inject constructor(
    private val tavilyService: TavilyService
) {

    /* ------------ Public API ------------- */

    suspend fun search(
        query: String,
        searchDepth: String = "basic",
        includeImages: Boolean = false,
        maxResults: Int = 10
    ): List<TavilySearchResult> {
        val request = TavilySearchRequest(
            query = query,
            search_depth = searchDepth,
            include_images = includeImages,
            max_results = maxResults
        )
        return tavilyService.search(request).results
    }

    suspend fun fetchMentalHealthResearch(
        maxResults: Int = 10
    ): List<TavilySearchResult> = try {
        val results = tavilyService.search(
            TavilySearchRequest(
                query = "latest mental health research papers 2025",
                max_results = maxResults
            )
        ).results
        Log.d(TAG, "Fetched ${results.size} research papers")
        results
    } catch (e: HttpException) {
        val body = e.response()?.errorBody()?.string()
        Log.e(TAG, "HTTP ${e.code()} – $body", e)
        throw e
    } catch (e: IOException) {
        Log.e(TAG, "Network error: ${e.message}", e)
        throw e
    } catch (e: Exception) {
        Log.e(TAG, "Unexpected error: ${e.message}", e)
        throw e
    }

    suspend fun extractArticleContent(url: String): ArticleContent {
        val response = tavilyService.extract(TavilyExtractRequest(listOf(url)))
        val extraction = response.extractions.firstOrNull()
            ?: throw IOException("No extraction for $url")

        extraction.error?.let { throw IOException("Extraction error: $it") }

        return ArticleContent(
            title = extraction.title,
            content = extraction.content,
            url = extraction.url
        )
    }

    companion object { private const val TAG = "TavilyRepository" }
}

/* ------------ Local DTOs / mappers ------------- */

data class ResearchPaper(
    val title: String,
    val snippet: String,
    val url: String,
    val score: Double?,
    val imageUrl: String?
)

data class ArticleContent(
    val title: String,
    val content: String,
    val url: String
)

/** Maps API model to domain model. */
private fun TavilySearchResult.toDomainModel() = ResearchPaper(
    title,
    content ?: "No description available",
    url,
    score,
    imageUrl
)