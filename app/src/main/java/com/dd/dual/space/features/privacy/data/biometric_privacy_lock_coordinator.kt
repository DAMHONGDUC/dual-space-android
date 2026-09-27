package com.dd.dual.space.features.privacy.data

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.dd.dual.space.R
import com.dd.dual.space.core.logging.AppLogger

class BiometricPrivacyLockCoordinator(
    private val activity: FragmentActivity,
    private val onUnlocked: () -> Unit,
) {
    fun canAuthenticate(): Boolean = BiometricManager.from(activity).canAuthenticate(privacyLockAuthenticators) == BiometricManager.BIOMETRIC_SUCCESS

    fun authenticate() {
        AppLogger.action("authenticate_privacy_lock", emptyMap())
        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    AppLogger.success("authenticate_privacy_lock", emptyMap())
                    onUnlocked()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    AppLogger.error("authenticate_privacy_lock", IllegalStateException("code=$errorCode"), emptyMap())
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    AppLogger.error("authenticate_privacy_lock", IllegalStateException("not_recognized"), emptyMap())
                }
            },
        )
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(activity.getString(R.string.privacy_lock_title))
                .setSubtitle(activity.getString(R.string.privacy_lock_prompt))
                .setAllowedAuthenticators(privacyLockAuthenticators)
                .build(),
        )
    }
}
