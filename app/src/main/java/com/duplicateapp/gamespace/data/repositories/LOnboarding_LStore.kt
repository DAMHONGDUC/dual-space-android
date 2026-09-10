package com.duplicateapp.gamespace.features.onboarding.data

import android.content.Context
import androidx.core.content.edit

class onboarding_store(context: Context) {
    private val preferences = context.getSharedPreferences(preferences_name, Context.MODE_PRIVATE)

    fun is_complete(): Boolean = preferences.getBoolean(complete_key, false)

    fun complete() {
        preferences.edit { putBoolean(complete_key, true) }
    }

    private companion object {
        const val preferences_name = "onboarding"
        const val complete_key = "complete"
    }
}
