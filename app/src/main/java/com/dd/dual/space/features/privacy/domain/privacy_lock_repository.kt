package com.dd.dual.space.features.privacy.domain

interface PrivacyLockRepository {
    fun isAvailable(): Boolean
    fun isEnabled(): Boolean
    fun setEnabled(isEnabled: Boolean)
}
