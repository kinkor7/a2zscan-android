package com.a2z.scan.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val A2ZDarkBlue = Color(0xFF04194E)
val A2ZGold = Color(0xFFCBBA8D)
val A2ZWhite = Color(0xFFFFFFFF)
val A2ZSoftGray = Color(0xFFF5F5F5)

// Secondary & surface tones
val A2ZSurfaceVariant = Color(0xFFE8EAF0)
val A2ZDarkSurface = Color(0xFF0A1224)
val A2ZDarkCard = Color(0xFF131D36)

private val LightColorScheme = lightColorScheme(
    primary = A2ZDarkBlue,
    onPrimary = A2ZWhite,
    primaryContainer = Color(0xFFE0E6F8),
    onPrimaryContainer = A2ZDarkBlue,
    secondary = A2ZGold,
    onSecondary = A2ZDarkBlue,
    secondaryContainer = Color(0xFFF3EFE4),
    onSecondaryContainer = Color(0xFF53451A),
    tertiary = A2ZDarkBlue,
    background = A2ZWhite,
    onBackground = A2ZDarkBlue,
    surface = A2ZWhite,
    onSurface = A2ZDarkBlue,
    surfaceVariant = A2ZSoftGray,
    onSurfaceVariant = Color(0xFF49454F)
)

private val DarkColorScheme = darkColorScheme(
    primary = A2ZGold,
    onPrimary = A2ZDarkBlue,
    primaryContainer = Color(0xFF1E2F5B),
    onPrimaryContainer = A2ZGold,
    secondary = A2ZGold,
    onSecondary = A2ZDarkBlue,
    secondaryContainer = Color(0xFF382F16),
    onSecondaryContainer = Color(0xFFF3EFE4),
    tertiary = A2ZGold,
    background = A2ZDarkSurface,
    onBackground = A2ZWhite,
    surface = A2ZDarkCard,
    onSurface = A2ZWhite,
    surfaceVariant = Color(0xFF1A2645),
    onSurfaceVariant = Color(0xFFC4C6D0)
)

@Composable
fun A2ZScanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
