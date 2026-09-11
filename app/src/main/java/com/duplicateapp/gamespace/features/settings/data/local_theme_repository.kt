package com.duplicateapp.gamespace.features.settings.data

import android.content.Context
import com.duplicateapp.gamespace.core.logging.AppLogger
import com.duplicateapp.gamespace.features.settings.domain.ThemeMode
import com.duplicateapp.gamespace.features.settings.domain.ThemeRepository

class LocalThemeRepository(context: Context) : ThemeRepository {
    private val preferences = context.applicationContext.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)

    override fun load(): ThemeMode {
        val storedMode: String = preferences.getString(themeModeKey, ThemeMode.system.name) ?: ThemeMode.system.name
        return ThemeMode.entries.firstOrNull { mode -> mode.name == storedMode } ?: ThemeMode.system
    }

    override fun save(themeMode: ThemeMode) {
        AppLogger.action("save_theme", mapOf("themeMode" to themeMode.name))
        preferences.edit().putString(themeModeKey, themeMode.name).apply()
        AppLogger.success("save_theme", mapOf("themeMode" to themeMode.name))
    }

    private companion object {
        const val preferencesName = "parallel_app_theme"
        const val themeModeKey = "theme_mode"
    }
}
