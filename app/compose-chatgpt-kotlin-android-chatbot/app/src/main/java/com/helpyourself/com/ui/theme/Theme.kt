package com.helpyourself.com.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val BackGroundColor = Color(0xFF343541)
val BackGroundMessageColor = Color(0xFF444654)
val TextFieldBackgroundColor = Color(0xFF40414F)
val TextFieldTextColor = Color.White
val ButtonBackgroundColor = Color(0xFF19C37D)
val ButtonTextColor = Color.White

private val DarkColorScheme = darkColorScheme(
    primary = ButtonBackgroundColor,
    onPrimary = ButtonTextColor,
    secondary = Color(0xFF19C37D),
    background = BackGroundColor,
    surface = BackGroundMessageColor,
    onSurface = Color.White,
    onSurfaceVariant = Color.White.copy(alpha = 0.7f),
    primaryContainer = Color(0xFF2A2B32),
)

private val LightColorScheme = lightColorScheme(
    primary = ButtonBackgroundColor,
    onPrimary = ButtonTextColor,
    secondary = Color(0xFF19C37D),
    background = Color.White,
    surface = Color(0xFFF7F7F8),
    onSurface = Color.Black,
    onSurfaceVariant = Color.Black.copy(alpha = 0.7f),
    primaryContainer = Color(0xFFEBEBF0),
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
} 