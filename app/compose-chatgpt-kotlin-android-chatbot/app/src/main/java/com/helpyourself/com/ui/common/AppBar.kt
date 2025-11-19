package com.helpyourself.com.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    currentRoute: String,
    onMenuClick: () -> Unit
) {
    Surface(
        shadowElevation = 4.dp,
        tonalElevation = 0.dp,
    ) {
        CenterAlignedTopAppBar(
            title = {
                Box {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = rememberAsyncImagePainter("https://raw.githubusercontent.com/lambiengcode/compose-chatgpt-kotlin-android-chatbot/main/images/app_icon.png"),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(32.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop,
                            contentDescription = null
                        )
                        Text(
                            text = when (currentRoute) {
                                AppRoute.Conversation.route -> "Chat"
                                AppRoute.AcademicResources.route -> "Academic Resources"
                                AppRoute.MentalHealthResources.route -> "Mental Health Resources"
                                AppRoute.Settings.route -> "Settings"
                                else -> "Help Yourself"
                            },
                            textAlign = TextAlign.Center,
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        Icons.Default.Menu,
                        contentDescription = "Menu",
                        modifier = Modifier.size(26.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
            ),
        )
    }
} 