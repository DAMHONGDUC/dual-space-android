package com.duplicateapp.gamespace.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val gameSpaceColorScheme = darkColorScheme(
    primary = GameSpaceColors.primaryBright,
    onPrimary = GameSpaceColors.background,
    secondary = GameSpaceColors.accent,
    background = GameSpaceColors.background,
    onBackground = GameSpaceColors.textPrimary,
    surface = GameSpaceColors.surface,
    onSurface = GameSpaceColors.textPrimary,
    surfaceVariant = GameSpaceColors.surfaceMuted,
    onSurfaceVariant = GameSpaceColors.textMuted,
    error = GameSpaceColors.accent,
)

@Composable
fun gameSpaceTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = gameSpaceColorScheme,
        content = content,
    )
}
