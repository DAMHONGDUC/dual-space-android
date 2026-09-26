package com.dd.dual.space.features.settings.domain

interface LaunchPreferencesRepository {
    fun confirmBeforeLaunch(): Boolean
    fun setConfirmBeforeLaunch(isEnabled: Boolean)
}
