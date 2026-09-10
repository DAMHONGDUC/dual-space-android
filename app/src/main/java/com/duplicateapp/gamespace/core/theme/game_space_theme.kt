package com.duplicateapp.gamespace.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val game_space_color_scheme = darkColorScheme(
    primary = game_space_colors.primary_bright,
    onPrimary = game_space_colors.background,
    secondary = game_space_colors.accent,
    background = game_space_colors.background,
    onBackground = game_space_colors.text_primary,
    surface = game_space_colors.surface,
    onSurface = game_space_colors.text_primary,
    surfaceVariant = game_space_colors.surface_muted,
    onSurfaceVariant = game_space_colors.text_muted,
    error = game_space_colors.accent,
)

@Composable
fun game_space_theme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = game_space_color_scheme,
        content = content,
    )
}
