package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val JarvisColorScheme = darkColorScheme(
    primary = StarkArcCyan,
    onPrimary = StarkDarkBg,
    primaryContainer = StarkSurfaceVariant,
    onPrimaryContainer = StarkArcCyan,
    secondary = StarkArcBlue,
    onSecondary = StarkDarkBg,
    secondaryContainer = StarkSurfaceElevated,
    onSecondaryContainer = StarkTextPrimary,
    tertiary = StarkCoreGold,
    onTertiary = StarkDarkBg,
    tertiaryContainer = StarkCoreGoldDim,
    onTertiaryContainer = StarkCoreGold,
    background = StarkDarkBg,
    onBackground = StarkTextPrimary,
    surface = StarkSurface,
    onSurface = StarkTextPrimary,
    surfaceVariant = StarkSurfaceVariant,
    onSurfaceVariant = StarkTextSecondary,
    outline = StarkBorder,
    outlineVariant = StarkBorderBright,
    error = StarkCrimson,
    onError = StarkDarkBg
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = JarvisColorScheme,
        typography = Typography,
        content = content
    )
}
