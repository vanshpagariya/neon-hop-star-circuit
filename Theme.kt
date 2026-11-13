package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NeonHopColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = SpaceDarkBg,
    primaryContainer = NeonCyanDim,
    onPrimaryContainer = TextPrimary,
    secondary = NeonMagenta,
    onSecondary = SpaceDarkBg,
    secondaryContainer = SpaceSurface,
    onSecondaryContainer = TextPrimary,
    tertiary = NeonPurple,
    onTertiary = SpaceDarkBg,
    background = SpaceDarkBg,
    onBackground = TextPrimary,
    surface = SpaceCardBg,
    onSurface = TextPrimary,
    surfaceVariant = SpaceSurface,
    onSurfaceVariant = TextSecondary,
    outline = SpaceCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep game's custom neon aesthetic
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NeonHopColorScheme,
        typography = Typography,
        content = content
    )
}
