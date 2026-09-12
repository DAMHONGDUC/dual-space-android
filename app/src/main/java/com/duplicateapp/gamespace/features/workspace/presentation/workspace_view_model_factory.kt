package com.duplicateapp.gamespace.features.workspace.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.duplicateapp.gamespace.BuildConfig
import com.duplicateapp.gamespace.features.auth.data.FirebaseAuthRepository
import com.duplicateapp.gamespace.features.auth.data.GoogleCredentialProvider
import com.duplicateapp.gamespace.features.onboarding.data.OnboardingStore
import com.duplicateapp.gamespace.features.premium.data.RevenueCatPremiumRepository
import com.duplicateapp.gamespace.features.premium.data.RevenueCatPurchaseManager
import com.duplicateapp.gamespace.features.settings.data.LocalLanguageRepository
import com.duplicateapp.gamespace.features.settings.data.LocalThemeRepository
import com.duplicateapp.gamespace.features.workspace.data.AndroidGameCatalog
import com.duplicateapp.gamespace.features.workspace.data.AndroidProfileProvisioner
import com.duplicateapp.gamespace.features.workspace.data.AndroidSettingsNavigator
import com.duplicateapp.gamespace.features.workspace.data.GameLauncherProvider
import com.duplicateapp.gamespace.features.workspace.data.PersistentWorkspaceRepository
import com.duplicateapp.gamespace.features.workspace.data.AndroidManagedProfileResolver
import com.duplicateapp.gamespace.features.workspace.data.AndroidDiagnosticReporter
import com.duplicateapp.gamespace.features.workspace.data.AndroidWorkspaceShortcutPublisher
import com.duplicateapp.gamespace.features.privacy.data.LocalPrivacyLockRepository

class WorkspaceViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(WorkspaceViewModel::class.java))
        val profileResolver = AndroidManagedProfileResolver(context)
        return WorkspaceViewModel(
            repository = PersistentWorkspaceRepository(context),
            gameLauncher = GameLauncherProvider.create(context, profileResolver),
            profileProvisioner = AndroidProfileProvisioner(context, profileResolver),
            gameCatalog = AndroidGameCatalog(context, profileResolver),
            onboardingStore = OnboardingStore(context),
            settingsNavigator = AndroidSettingsNavigator(context),
            themeRepository = LocalThemeRepository(context),
            languageRepository = LocalLanguageRepository(context),
            authRepository = FirebaseAuthRepository(context),
            premiumRepository = RevenueCatPremiumRepository(
                context,
                BuildConfig.REVENUECAT_API_KEY,
                BuildConfig.REVENUECAT_ENTITLEMENT_ID,
            ),
            googleCredentialProvider = GoogleCredentialProvider(BuildConfig.FIREBASE_WEB_CLIENT_ID),
            purchaseManager = RevenueCatPurchaseManager(BuildConfig.REVENUECAT_ENTITLEMENT_ID),
            shortcutPublisher = AndroidWorkspaceShortcutPublisher(context),
            diagnosticReporter = AndroidDiagnosticReporter(context),
            privacyLockRepository = LocalPrivacyLockRepository(context),
        ) as T
    }
}
