package com.duplicateapp.gamespace.features.settings.domain

interface ThemeRepository {
    fun load(): ThemeMode
    fun save(themeMode: ThemeMode)
}
