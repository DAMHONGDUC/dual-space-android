package com.duplicateapp.gamespace.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val parallelAppColorScheme = darkColorScheme(
    primary = ParallelAppColors.primaryBright,
    onPrimary = ParallelAppColors.background,
    secondary = ParallelAppColors.accent,
    background = ParallelAppColors.background,
    onBackground = ParallelAppColors.textPrimary,
    surface = ParallelAppColors.surface,
    onSurface = ParallelAppColors.textPrimary,
    surfaceVariant = ParallelAppColors.surfaceMuted,
    onSurfaceVariant = ParallelAppColors.textMuted,
    error = ParallelAppColors.accent,
)

@Composable
fun parallelAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = parallelAppColorScheme,
        content = content,
    )
}
