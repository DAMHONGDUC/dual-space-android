package com.dd.dual.space.features.settings.data

import android.content.Context
import com.dd.dual.space.core.logging.AppLogger
import com.dd.dual.space.features.settings.domain.LaunchPreferencesRepository

class LocalLaunchPreferencesRepository(context: Context) : LaunchPreferencesRepository {
    private val preferences = context.applicationContext.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)

    override fun confirmBeforeLaunch(): Boolean = preferences.getBoolean(confirmBeforeLaunchKey, true)

    override fun setConfirmBeforeLaunch(isEnabled: Boolean) {
        AppLogger.action("save_launch_confirmation", mapOf("isEnabled" to isEnabled))
        preferences.edit().putBoolean(confirmBeforeLaunchKey, isEnabled).apply()
        AppLogger.success("save_launch_confirmation", mapOf("isEnabled" to isEnabled))
    }

    private companion object {
        const val preferencesName = "parallel_app_launch"
        const val confirmBeforeLaunchKey = "confirm_before_launch"
    }
}
