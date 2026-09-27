package com.dd.dual.space.features.privacy.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.dd.dual.space.core.logging.AppLogger
import com.dd.dual.space.features.privacy.domain.PrivacyLockRepository
import androidx.biometric.BiometricManager

class LocalPrivacyLockRepository(context: Context) : PrivacyLockRepository {
    private val preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
    private val biometricManager: BiometricManager = BiometricManager.from(context)

    override fun isAvailable(): Boolean = biometricManager.canAuthenticate(privacyLockAuthenticators) == BiometricManager.BIOMETRIC_SUCCESS

    override fun isEnabled(): Boolean = preferences.getBoolean(enabledKey, false)

    override fun setEnabled(isEnabled: Boolean) {
        AppLogger.action("set_privacy_lock", mapOf("enabled" to isEnabled))
        preferences.edit { putBoolean(enabledKey, isEnabled) }
        AppLogger.success("set_privacy_lock", mapOf("enabled" to isEnabled))
    }

    // SharedPreferences keeps listeners weakly, so the caller must hold the returned listener.
    fun observeEnabled(onChanged: (Boolean) -> Unit): SharedPreferences.OnSharedPreferenceChangeListener {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == enabledKey) onChanged(isEnabled())
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        return listener
    }

    fun stopObserving(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        preferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    private companion object {
        const val preferencesName: String = "privacy_lock"
        const val enabledKey: String = "enabled_v1"
    }
}
