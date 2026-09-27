package com.dd.dual.space.core.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.dd.dual.space.features.settings.domain.ThemeMode

private val darkParallelAppColorScheme = darkColorScheme(
    primary = ParallelAppColors.darkPrimary,
    onPrimary = ParallelAppColors.darkOnPrimary,
    secondary = ParallelAppColors.darkSecondary,
    background = ParallelAppColors.darkBackground,
    onBackground = ParallelAppColors.darkTextPrimary,
    surface = ParallelAppColors.darkSurface,
    onSurface = ParallelAppColors.darkTextPrimary,
    surfaceVariant = ParallelAppColors.darkSurfaceMuted,
    onSurfaceVariant = ParallelAppColors.darkTextMuted,
    error = ParallelAppColors.darkSecondary,
)

private val lightParallelAppColorScheme = lightColorScheme(
    primary = ParallelAppColors.lightPrimary,
    onPrimary = ParallelAppColors.lightOnPrimary,
    secondary = ParallelAppColors.lightSecondary,
    background = ParallelAppColors.lightBackground,
    onBackground = ParallelAppColors.lightTextPrimary,
    surface = ParallelAppColors.lightSurface,
    onSurface = ParallelAppColors.lightTextPrimary,
    surfaceVariant = ParallelAppColors.lightSurfaceMuted,
    onSurfaceVariant = ParallelAppColors.lightTextMuted,
    error = ParallelAppColors.lightSecondary,
)

@Composable
fun parallelAppTheme(themeMode: ThemeMode, content: @Composable () -> Unit) {
    val useDarkTheme: Boolean = when (themeMode) {
        ThemeMode.system -> isSystemInDarkTheme()
        ThemeMode.light -> false
        ThemeMode.dark -> true
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !useDarkTheme
                isAppearanceLightNavigationBars = !useDarkTheme
            }
        }
    }
    MaterialTheme(
        colorScheme = if (useDarkTheme) darkParallelAppColorScheme else lightParallelAppColorScheme,
        content = content,
    )
}
