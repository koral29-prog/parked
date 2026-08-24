package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Parchment, onPrimary = Ink,
    primaryContainer = ObsidianCard, onPrimaryContainer = Parchment,
    secondary = Stone, onSecondary = Ink,
    secondaryContainer = ObsidianRaised, onSecondaryContainer = Stone,
    tertiary = Parchment, onTertiary = Ink,
    background = Obsidian, onBackground = Parchment,
    surface = ObsidianSurface, onSurface = Parchment,
    surfaceVariant = ObsidianRaised, onSurfaceVariant = Stone,
    outline = StoneDim, outlineVariant = Hairline,
    error = SafetyRed, errorContainer = Color(0xFF93000A), onError = Color(0xFF690005)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF242321), onPrimary = Color.White,
    primaryContainer = Color(0xFFF0EEE9), onPrimaryContainer = Ink,
    secondary = Color(0xFF625F58), onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8E4DC), onSecondaryContainer = Ink,
    tertiary = Color(0xFF242321), onTertiary = Color.White,
    background = Color(0xFFFFFDF8), onBackground = Ink,
    surface = Color(0xFFFFFDF8), onSurface = Ink,
    surfaceVariant = Color(0xFFF1EFEA), onSurfaceVariant = Color(0xFF625F58),
    outline = Color(0xFF77746D), outlineVariant = Color(0xFFD2CEC6),
    error = CleanError, errorContainer = CleanErrorContainerLight, onError = Color.White
)

@Composable
fun ParkedTheme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
