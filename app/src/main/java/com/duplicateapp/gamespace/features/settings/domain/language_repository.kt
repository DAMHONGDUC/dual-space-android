package com.duplicateapp.gamespace.features.settings.domain

import android.content.Context

interface LanguageRepository {
    fun load(): AppLanguage
    fun save(language: AppLanguage)
    fun localizedContext(baseContext: Context): Context
}
