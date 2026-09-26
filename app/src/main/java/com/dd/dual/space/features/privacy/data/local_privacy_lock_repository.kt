package com.dd.dual.space.features.privacy.data

import android.content.Context
import androidx.core.content.edit
import com.dd.dual.space.core.logging.AppLogger
import com.dd.dual.space.features.privacy.domain.PrivacyLockRepository
import androidx.biometric.BiometricManager

class LocalPrivacyLockRepository(context: Context) : PrivacyLockRepository {
    private val preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
    private val biometricManager: BiometricManager = BiometricManager.from(context)

    override fun isAvailable(): Boolean = biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS

    override fun isEnabled(): Boolean = preferences.getBoolean(enabledKey, false)

    override fun setEnabled(isEnabled: Boolean) {
        AppLogger.action("set_privacy_lock", mapOf("enabled" to isEnabled))
        preferences.edit { putBoolean(enabledKey, isEnabled) }
        AppLogger.success("set_privacy_lock", mapOf("enabled" to isEnabled))
    }

    private companion object {
        const val preferencesName: String = "privacy_lock"
        const val enabledKey: String = "enabled_v1"
        const val authenticators: Int = BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
    }
}
