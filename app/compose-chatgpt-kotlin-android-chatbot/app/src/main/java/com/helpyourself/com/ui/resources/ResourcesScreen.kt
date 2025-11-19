package com.helpyourself.com.ui.resources

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helpyourself.com.data.repository.TavilyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResourcesViewModel @Inject constructor(
    private val tavilyRepository: TavilyRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<ResourcesUiState>(ResourcesUiState.Initial)
    val uiState: StateFlow<ResourcesUiState> = _uiState

    init {
        // Automatically fetch resources when ViewModel is created
        fetchMentalHealthResources()
    }

    fun fetchMentalHealthResources() {
        viewModelScope.launch {
            _uiState.value = ResourcesUiState.Loading
            try {
                // Pre-curated query for mental health resources in India
                val results = tavilyRepository.search(
                    query = "latest academic research books and papers on mental health in India",
                    searchDepth = "advanced",
                    includeImages = false,
                    maxResults = 15
                )
                
                _uiState.value = if (results.isEmpty()) {
                    ResourcesUiState.Empty
                } else {
                    // Categorize results as books or papers based on content
                    val categorizedResults = results.map { result ->
                        val isBook = result.title.contains("book", ignoreCase = true) || 
                                    result.content?.contains("book", ignoreCase = true) == true ||
                                    result.url.contains("book", ignoreCase = true) ||
                                    !result.url.contains("pdf", ignoreCase = true)
                        
                        SearchResult(
                            title = result.title,
                            content = result.content ?: "No description available",
                            url = result.url,
                            type = if (isBook) "books" else "papers"
                        )
                    }
                    ResourcesUiState.Success(categorizedResults)
                }
            } catch (e: retrofit2.HttpException) {
                val errorMessage = when (e.code()) {
                    401 -> "API key invalid or expired. Please check your Tavily API credentials."
                    403 -> "Access forbidden. Your API key may not have permission to access this resource."
                    429 -> "Too many requests. API rate limit exceeded."
                    500, 502, 503, 504 -> "Tavily server error. Please try again later."
                    else -> "Error ${e.code()}: ${e.message()}"
                }
                _uiState.value = ResourcesUiState.Error(errorMessage)
            } catch (e: java.net.UnknownHostException) {
                _uiState.value = ResourcesUiState.Error("Network error: Unable to reach Tavily API. Check your internet connection.")
            } catch (e: java.io.IOException) {
                _uiState.value = ResourcesUiState.Error("Network error: ${e.message ?: "Connection problem"}")
            } catch (e: Exception) {
                _uiState.value = ResourcesUiState.Error("Failed to load resources: ${e.message ?: "Unknown error"}")
            }
        }
    }

    fun retryFetch() {
        fetchMentalHealthResources()
    }
}

sealed class ResourcesUiState {
    object Initial : ResourcesUiState()
    object Loading : ResourcesUiState()
    object Empty : ResourcesUiState()
    data class Success(val results: List<SearchResult>) : ResourcesUiState()
    data class Error(val message: String) : ResourcesUiState()
}

data class SearchResult(
    val title: String,
    val content: String,
    val url: String,
    val type: String
)

@Composable
fun ResourcesScreen(
    viewModel: ResourcesViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val uriHandler = LocalUriHandler.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Academic Resources",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Latest research and books on mental health in India",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            when (val state = uiState) {
                ResourcesUiState.Initial, ResourcesUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Loading academic resources...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                ResourcesUiState.Empty -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No resources found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.retryFetch() }) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Retry")
                            }
                        }
                    }
                }
                is ResourcesUiState.Success -> {
                    val books = state.results.filter { it.type == "books" }
                    val papers = state.results.filter { it.type == "papers" }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (books.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Books",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            
                            items(books) { result ->
                                ResourceCard(
                                    title = result.title,
                                    description = result.content,
                                    type = result.type,
                                    onClick = { uriHandler.openUri(result.url) }
                                )
                            }
                        }

                        if (papers.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Research Papers",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                                )
                            }
                            
                            items(papers) { result ->
                                ResourceCard(
                                    title = result.title,
                                    description = result.content,
                                    type = result.type,
                                    onClick = { uriHandler.openUri(result.url) }
                                )
                            }
                        }
                    }
                }
                is ResourcesUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.retryFetch() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourceCard(
    title: String,
    description: String,
    type: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (type == "books") Icons.Default.MenuBook else Icons.Default.Article,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Click to read more",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
} 