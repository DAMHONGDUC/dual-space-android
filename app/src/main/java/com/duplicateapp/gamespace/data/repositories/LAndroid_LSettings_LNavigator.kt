package com.duplicateapp.gamespace.features.workspace.data

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.duplicateapp.gamespace.core.logging.app_logger
import com.duplicateapp.gamespace.features.workspace.domain.settings_navigator

class android_settings_navigator(private val context: Context) : settings_navigator {
    override fun open_android_settings(): Boolean {
        app_logger.action("open_android_settings", emptyMap())
        return try {
            context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            app_logger.success("open_android_settings", emptyMap())
            true
        } catch (error: Exception) {
            app_logger.error("open_android_settings", error, emptyMap())
            false
        }
    }
}
