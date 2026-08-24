package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CleanGreenLight,
    onPrimary = Color.White,
    primaryContainer = CleanGreenContainerDark,
    onPrimaryContainer = CleanTextPrimaryDark,
    secondary = CleanSlateOlive,
    onSecondary = Color.White,
    secondaryContainer = CleanSurfaceElevatedDark,
    onSecondaryContainer = CleanTextPrimaryDark,
    tertiary = CleanAmber,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF5E3900),
    onTertiaryContainer = Color(0xFFFFDDB3),
    background = CleanSurfaceDark,
    onBackground = CleanTextPrimaryDark,
    surface = CleanSurfaceDark,
    onSurface = CleanTextPrimaryDark,
    surfaceVariant = CleanSurfaceCardDark,
    onSurfaceVariant = CleanTextSecondaryDark,
    outline = CleanBorderDark,
    outlineVariant = Color(0xFF2D352B),
    error = CleanError,
    errorContainer = CleanErrorContainerDark,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = CleanGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = CleanGreenContainerLight,
    onPrimaryContainer = CleanGreenOnContainer,
    secondary = CleanSlateOlive,
    onSecondary = Color.White,
    secondaryContainer = CleanGreenContainerInner,
    onSecondaryContainer = CleanSlateOlive,
    tertiary = CleanAmberDark,
    onTertiary = Color.White,
    tertiaryContainer = CleanAmberContainer,
    onTertiaryContainer = CleanAmberDark,
    background = CleanSurfaceLight,
    onBackground = CleanTextPrimaryLight,
    surface = CleanSurfaceLight,
    onSurface = CleanTextPrimaryLight,
    surfaceVariant = CleanSurfaceCardLight,
    onSurfaceVariant = CleanTextSecondaryLight,
    outline = CleanBorderDashed,
    outlineVariant = CleanBorderSubtle,
    error = CleanError,
    errorContainer = CleanErrorContainerLight,
    onError = Color.White
)

@Composable
fun ParkedTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
