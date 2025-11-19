package com.helpyourself.com.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

object ThemeState {
    var isDarkTheme by mutableStateOf(false)
}

val LocalThemeState = staticCompositionLocalOf { ThemeState } 