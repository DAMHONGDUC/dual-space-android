package com.duplicateapp.gamespace.features.onboarding.data

import android.content.Context
import androidx.core.content.edit

class OnboardingStore(context: Context) {
    private val preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)

    fun isComplete(): Boolean = preferences.getBoolean(completeKey, false)

    fun complete() {
        preferences.edit { putBoolean(completeKey, true) }
    }

    private companion object {
        const val preferencesName = "onboarding"
        const val completeKey = "complete"
    }
}
