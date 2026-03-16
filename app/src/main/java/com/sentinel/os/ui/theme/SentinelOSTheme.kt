package com.sentinel.os.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// AMOLED Dark Colors
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF1F88E5),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF0D47A1),
    onPrimaryContainer = Color(0xFFE3F2FD),
    secondary = Color(0xFF00BCD4),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF00838F),
    onSecondaryContainer = Color(0xFFB2EBF2),
    tertiary = Color(0xFF4CAF50),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF2E7D32),
    onTertiaryContainer = Color(0xFFC8E6C9),
    error = Color(0xFFEF5350),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFC62828),
    onErrorContainer = Color(0xFFFFEBEE),
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF0A0E27),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF1A1F3A),
    onSurfaceVariant = Color(0xFFB0BEC5),
    outline = Color(0xFF78909C),
    outlineVariant = Color(0xFF424242),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFECEDEE),
    inverseOnSurface = Color(0xFF11181C),
    inversePrimary = Color(0xFF0D47A1)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1F88E5),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = Color(0xFF0D47A1),
    secondary = Color(0xFF00BCD4),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFB2EBF2),
    onSecondaryContainer = Color(0xFF00838F),
    tertiary = Color(0xFF4CAF50),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFC8E6C9),
    onTertiaryContainer = Color(0xFF2E7D32),
    error = Color(0xFFEF5350),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFFC62828),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF11181C),
    surface = Color(0xFFFAFAFA),
    onSurface = Color(0xFF11181C),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF687076),
    outline = Color(0xFF9BA1A6),
    outlineVariant = Color(0xFFCAD1D8),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF11181C),
    inverseOnSurface = Color(0xFFECEDEE),
    inversePrimary = Color(0xFF1F88E5)
)

@Composable
fun SentinelOSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SentinelOSTypography,
        shapes = SentinelOSShapes,
        content = content
    )
}
