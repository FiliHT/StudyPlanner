package com.filiht.studyplanner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF5D5FEF),
    onPrimary = Color.White,
    background = Color(0xFF0A0C16), // Midnight Blue
    surface = Color(0xFF161B30),    // Navy Blue for cards
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFF9EA3AE),
    secondary = Color(0xFF00A389),
    outlineVariant = Color(0xFF252D4D)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF5D5FEF),
    onPrimary = Color.White,
    background = Color(0xFFF8F9FE),
    surface = Color.White,
    onSurface = Color(0xFF1A1C2E),
    onSurfaceVariant = Color(0xFF9EA3AE),
    secondary = Color(0xFF00A389),
    outlineVariant = Color(0xFFF0F0F0)
)

@Composable
fun StudyPlannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
