package com.duplicateapp.gamespace.features.workspace.data

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.duplicateapp.gamespace.core.logging.AppLogger
import com.duplicateapp.gamespace.features.workspace.domain.SettingsNavigator

class AndroidSettingsNavigator(private val context: Context) : SettingsNavigator {
    override fun openAndroidSettings(): Boolean {
        AppLogger.action("open_android_settings", emptyMap())
        return try {
            context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            AppLogger.success("open_android_settings", emptyMap())
            true
        } catch (error: Exception) {
            AppLogger.error("open_android_settings", error, emptyMap())
            false
        }
    }
}
