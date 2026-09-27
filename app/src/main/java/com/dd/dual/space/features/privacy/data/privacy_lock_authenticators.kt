package com.dd.dual.space.features.privacy.data

import android.os.Build
import androidx.biometric.BiometricManager.Authenticators

// AndroidX rejects BIOMETRIC_STRONG with DEVICE_CREDENTIAL before API 30. The lock only gates
// the workspace UI and never unlocks keys, so API 29 uses the combination it supports.
val privacyLockAuthenticators: Int =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Authenticators.BIOMETRIC_STRONG or Authenticators.DEVICE_CREDENTIAL
    } else {
        Authenticators.BIOMETRIC_WEAK or Authenticators.DEVICE_CREDENTIAL
    }
