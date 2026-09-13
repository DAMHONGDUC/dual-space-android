package com.duplicateapp.gamespace.features.settings.domain

interface LaunchPreferencesRepository {
    fun confirmBeforeLaunch(): Boolean
    fun setConfirmBeforeLaunch(isEnabled: Boolean)
}
