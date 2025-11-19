package com.helpyourself.com.data.api

import retrofit2.http.Body
import retrofit2.http.POST

interface TavilyService {

    @POST("search")
    suspend fun search(
        @Body request: TavilySearchRequest
    ): TavilySearchResponse

    @POST("extract")
    suspend fun extract(
        @Body request: TavilyExtractRequest
    ): TavilyExtractResponse
}

/* ---------- request / response models ---------- */
data class TavilySearchRequest(
    val query: String = "latest mental health research 2025",
    val search_depth: String = "basic",
    val include_images: Boolean = false,
    val max_results: Int = 10
)

data class TavilyExtractRequest(
    val urls: List<String>
)

data class TavilySearchResponse(
    val results: List<TavilySearchResult>
)

data class TavilyExtractResponse(
    val extractions: List<TavilyExtraction>
)

data class TavilyExtraction(
    val url: String,
    val title: String,
    val content: String,
    val error: String? = null
)

data class TavilySearchResult(
    val title: String,
    val content: String?,
    val url: String,
    val score: Double?,
    val imageUrl: String?
)