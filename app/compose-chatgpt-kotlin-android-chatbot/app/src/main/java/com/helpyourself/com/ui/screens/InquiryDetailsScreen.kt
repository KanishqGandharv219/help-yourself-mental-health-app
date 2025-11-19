package com.helpyourself.com.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

data class Organization(
    val name: String,
    val description: String,
    val phone: String,
    val email: String,
    val location: String,
    val services: List<String>,
    val isEmergency: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InquiryDetailsScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    val organizations = listOf(
        Organization(
            name = "NIMHANS",
            description = "National Institute of Mental Health and Neurosciences - Premier mental health institution under Ministry of Health and Family Welfare",
            phone = "080-26995000",
            email = "ms@nimhans.ac.in",
            location = "Hosur Road, Bengaluru, Karnataka - 560029",
            services = listOf("24/7 Psychiatric Emergency Care", "Specialized Mental Health Services", "Rehabilitation Services"),
            isEmergency = true
        ),
        Organization(
            name = "Central Institute of Psychiatry (CIP)",
            description = "One of India's central mental health institutions providing tertiary care services",
            phone = "0651-2451115",
            email = "teleparamarshcip@gmail.com",
            location = "Kanke, Ranchi, Jharkhand - 834006",
            services = listOf("24/7 Emergency Services", "Tele-Psychiatry Support", "Specialized Clinics"),
            isEmergency = true
        ),
        Organization(
            name = "LGBRIMH",
            description = "Lokopriya Gopinath Bordoloi Regional Institute of Mental Health - Regional mental health institute for northeastern states",
            phone = "03712-232652",
            email = "mail@lgbrimh.gov.in",
            location = "Tezpur, Assam - 784001",
            services = listOf("Emergency Psychiatric Care", "Mental Health Services", "Regional Support Center"),
            isEmergency = true
        ),
        Organization(
            name = "Tele MANAS",
            description = "National Tele Mental Health Programme providing 24/7 toll-free mental health services",
            phone = "14416",
            email = "",
            location = "All states and union territories",
            services = listOf("24/7 Toll-free Support", "Multi-language Counseling", "Mental Health Assistance"),
            isEmergency = true
        ),
        Organization(
            name = "KIRAN Mental Health Helpline",
            description = "Ministry of Social Justice and Empowerment's 24/7 mental health rehabilitation helpline",
            phone = "1800-599-0019",
            email = "",
            location = "National Coverage",
            services = listOf("24/7 Crisis Support", "13 Language Support", "Mental Health Rehabilitation"),
            isEmergency = true
        ),
        Organization(
            name = "The Live Love Laugh Foundation",
            description = "Mental health awareness and support organization",
            phone = "91-8025711088",
            email = "info@thelivelovelaughfoundation.org",
            location = "Bengaluru, Karnataka",
            services = listOf("Mental Health Education", "Support Programs", "Resources"),
            isEmergency = false
        ),
        Organization(
            name = "Sangath",
            description = "Community mental health research organization",
            phone = "91-8322904666",
            email = "contactus@sangath.in",
            location = "Goa",
            services = listOf("Research", "Community Programs", "Mental Health Services"),
            isEmergency = false
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mental Health Support") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Emergency Contacts Section
            item {
                Text(
                    text = "Emergency Support",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            items(organizations.filter { it.isEmergency }) { org ->
                EmergencyOrganizationCard(org)
            }
            
            // Regular Support Section
            item {
                Text(
                    text = "Additional Support",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }
            
            items(organizations.filter { !it.isEmergency }) { org ->
                RegularOrganizationCard(org)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmergencyOrganizationCard(org: Organization) {
    val context = LocalContext.current
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Organization Name
            Text(
                text = org.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Description
            Text(
                text = org.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Location
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "Location",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = org.location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Services
            org.services.forEach { service ->
                Text(
                    text = "• $service",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Contact Button - Only Call for Emergency
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledIconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${org.phone}")
                        }
                        context.startActivity(intent)
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Call,
                        contentDescription = "Call ${org.name}"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegularOrganizationCard(org: Organization) {
    val context = LocalContext.current
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Organization Name
            Text(
                text = org.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Description
            Text(
                text = org.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Location
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "Location",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = org.location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Services
            org.services.forEach { service ->
                Text(
                    text = "• $service",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Contact Buttons - Both Call and Email for Regular
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:${org.email}")
                        }
                        context.startActivity(intent)
                    },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = "Email ${org.name}",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                FilledIconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${org.phone}")
                        }
                        context.startActivity(intent)
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.Call,
                        contentDescription = "Call ${org.name}"
                    )
                }
            }
        }
    }
} 