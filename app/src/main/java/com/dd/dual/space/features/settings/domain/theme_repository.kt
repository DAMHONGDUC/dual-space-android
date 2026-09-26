package com.dd.dual.space.features.settings.domain

interface ThemeRepository {
    fun load(): ThemeMode
    fun save(themeMode: ThemeMode)
}
