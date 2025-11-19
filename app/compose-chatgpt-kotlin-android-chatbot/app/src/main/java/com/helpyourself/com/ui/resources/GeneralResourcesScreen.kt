package com.helpyourself.com.ui.resources

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralResourcesScreen() {
    val uriHandler = LocalUriHandler.current
    val resources = remember {
        listOf(
            GeneralResource(
                title = "Mindfulness Exercises",
                description = "Collection of guided mindfulness exercises and meditation techniques",
                icon = Icons.Default.SelfImprovement,
                type = "Wellness",
                url = "https://www.mindful.org/meditation/mindfulness-getting-started/"
            ),
            GeneralResource(
                title = "Stress Management Guide",
                description = "Comprehensive guide to managing stress and anxiety",
                icon = Icons.Default.Psychology,
                type = "Mental Health",
                url = "https://www.helpguide.org/articles/stress/stress-management.htm"
            ),
            GeneralResource(
                title = "Self-Help Techniques",
                description = "Practical self-help techniques for emotional well-being",
                icon = Icons.Default.Favorite,
                type = "Self-Help",
                url = "https://www.psychologytoday.com/us/basics/self-help"
            ),
            GeneralResource(
                title = "Crisis Support",
                description = "24/7 crisis support and helpline information",
                icon = Icons.Default.SupportAgent,
                type = "Support",
                url = "https://www.crisistextline.org/"
            ),
            GeneralResource(
                title = "Healthy Living Tips",
                description = "Tips for maintaining a healthy lifestyle and mental well-being",
                icon = Icons.Default.HealthAndSafety,
                type = "Lifestyle",
                url = "https://www.mentalhealth.gov/live-your-life-well"
            )
        )
    }

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
                text = "General Resources",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(resources) { resource ->
                    GeneralResourceCard(
                        resource = resource,
                        onClick = { uriHandler.openUri(resource.url) }
                    )
                }
            }
        }
    }
}

data class GeneralResource(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val type: String,
    val url: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralResourceCard(
    resource: GeneralResource,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = resource.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = resource.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = resource.type,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = resource.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open resource",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
} 