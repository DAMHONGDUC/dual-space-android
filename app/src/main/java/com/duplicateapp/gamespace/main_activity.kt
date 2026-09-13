package com.duplicateapp.gamespace

import android.content.Context
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.duplicateapp.gamespace.features.ads.data.AdConsentManager
import com.duplicateapp.gamespace.features.ads.data.AdConsentState
import com.duplicateapp.gamespace.features.settings.data.LocalLanguageRepository
import com.duplicateapp.gamespace.features.update.data.ForceUpdateCoordinator
import androidx.fragment.app.FragmentActivity
import android.content.Intent
import android.view.WindowManager
import android.os.SystemClock
import com.duplicateapp.gamespace.core.logging.AppLogger
import com.duplicateapp.gamespace.features.privacy.data.BiometricPrivacyLockCoordinator
import com.duplicateapp.gamespace.features.privacy.data.LocalPrivacyLockRepository

class MainActivity : FragmentActivity() {
    private var adConsentState by mutableStateOf(AdConsentState())
    private var isForceUpdateRequired by mutableStateOf(false)
    private lateinit var adConsentManager: AdConsentManager
    private lateinit var forceUpdateCoordinator: ForceUpdateCoordinator
    private lateinit var privacyLockRepository: LocalPrivacyLockRepository
    private lateinit var privacyLockCoordinator: BiometricPrivacyLockCoordinator
    private var isPrivacyLocked by mutableStateOf(false)
    private var shortcutSessionId by mutableStateOf<String?>(null)
    private val updateLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
        if (result.resultCode != RESULT_OK) isForceUpdateRequired = true
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocalLanguageRepository(newBase).localizedContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val startedAtMillis: Long = SystemClock.elapsedRealtime()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        adConsentManager = AdConsentManager(this)
        privacyLockRepository = LocalPrivacyLockRepository(this)
        updateScreenCaptureProtection()
        privacyLockCoordinator = BiometricPrivacyLockCoordinator(this) { isPrivacyLocked = false }
        isPrivacyLocked = privacyLockRepository.isEnabled()
        shortcutSessionId = intent.getStringExtra(shortcutSessionIdExtra)
        forceUpdateCoordinator = ForceUpdateCoordinator(this, updateLauncher) { required -> isForceUpdateRequired = required }
        setContent {
            parallelAppApp(
                canRequestAds = adConsentState.canRequestAds,
                privacyOptionsRequired = adConsentState.privacyOptionsRequired,
                isForceUpdateRequired = isForceUpdateRequired,
                onOpenPrivacyOptions = { adConsentManager.showPrivacyOptions(::updateAdConsentState) },
                onUpdate = forceUpdateCoordinator::start,
                onCloseForUpdate = ::finishAffinity,
                isPrivacyLocked = isPrivacyLocked,
                shortcutSessionId = shortcutSessionId,
                onShortcutConsumed = { shortcutSessionId = null },
                onUnlock = privacyLockCoordinator::authenticate,
            )
        }
        window.decorView.post {
            AppLogger.success(
                "startup_first_frame_ready",
                mapOf("durationMs" to SystemClock.elapsedRealtime() - startedAtMillis),
            )
            adConsentManager.request(::updateAdConsentState)
            forceUpdateCoordinator.check()
        }
    }

    override fun onResume() {
        super.onResume()
        updateScreenCaptureProtection()
        if (::forceUpdateCoordinator.isInitialized && window.decorView.isLaidOut) forceUpdateCoordinator.check()
        if (::privacyLockRepository.isInitialized && privacyLockRepository.isEnabled() && isPrivacyLocked) {
            privacyLockCoordinator.authenticate()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::privacyLockRepository.isInitialized && privacyLockRepository.isEnabled() && !isChangingConfigurations) {
            isPrivacyLocked = true
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        shortcutSessionId = intent.getStringExtra(shortcutSessionIdExtra)
    }

    private fun updateAdConsentState(state: AdConsentState) {
        adConsentState = state
    }

    private fun updateScreenCaptureProtection() {
        if (privacyLockRepository.isEnabled()) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    companion object {
        const val shortcutSessionIdExtra: String = "workspace_session_id"
    }
}
