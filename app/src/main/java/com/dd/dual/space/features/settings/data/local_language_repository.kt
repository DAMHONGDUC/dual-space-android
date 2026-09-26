package com.dd.dual.space.features.settings.data

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import com.dd.dual.space.core.logging.AppLogger
import com.dd.dual.space.features.settings.domain.AppLanguage
import com.dd.dual.space.features.settings.domain.LanguageRepository
import java.util.Locale

class LocalLanguageRepository(context: Context) : LanguageRepository {
    private val preferences = context.applicationContext.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)

    override fun load(): AppLanguage {
        val storedLanguage: String = preferences.getString(languageKey, AppLanguage.system.name) ?: AppLanguage.system.name
        return AppLanguage.entries.firstOrNull { language -> language.name == storedLanguage } ?: AppLanguage.system
    }

    override fun save(language: AppLanguage) {
        AppLogger.action("save_language", mapOf("language" to language.name))
        preferences.edit().putString(languageKey, language.name).apply()
        AppLogger.success("save_language", mapOf("language" to language.name))
    }

    override fun localizedContext(baseContext: Context): Context {
        val languageTag: String = load().languageTag ?: return baseContext
        val configuration = Configuration(baseContext.resources.configuration)
        configuration.setLocales(LocaleList.forLanguageTags(languageTag))
        configuration.setLayoutDirection(Locale.forLanguageTag(languageTag))
        return baseContext.createConfigurationContext(configuration)
    }

    private companion object {
        const val preferencesName = "parallel_app_language"
        const val languageKey = "language"
    }
}
