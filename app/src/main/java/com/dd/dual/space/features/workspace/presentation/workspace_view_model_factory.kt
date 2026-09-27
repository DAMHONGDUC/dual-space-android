package com.dd.dual.space.features.workspace.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dd.dual.space.features.onboarding.data.OnboardingStore
import com.dd.dual.space.features.settings.data.LocalLanguageRepository
import com.dd.dual.space.features.settings.data.LocalLaunchPreferencesRepository
import com.dd.dual.space.features.settings.data.LocalThemeRepository
import com.dd.dual.space.features.workspace.data.AndroidGameCatalog
import com.dd.dual.space.features.workspace.data.AndroidSettingsNavigator
import com.dd.dual.space.features.workspace.data.PersistentWorkspaceRepository
import com.dd.dual.space.features.workspace.data.AndroidDiagnosticReporter
import com.dd.dual.space.features.workspace.data.AndroidWorkspaceShortcutPublisher
import com.dd.dual.space.features.privacy.data.LocalPrivacyLockRepository
import com.dd.dual.space.features.workspace.data.VirtualProfileProvisioner
import com.dd.dual.space.features.workspace.data.VirtualizedGameLauncher
import com.dd.dual.space.features.virtualization.data.TheUniverseVirtualGameRuntime

class WorkspaceViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(WorkspaceViewModel::class.java))
        val virtualizedGameLauncher = VirtualizedGameLauncher(TheUniverseVirtualGameRuntime())
        return WorkspaceViewModel(
            repository = PersistentWorkspaceRepository(context),
            gameLauncher = virtualizedGameLauncher,
            gameCopyRemover = virtualizedGameLauncher,
            profileProvisioner = VirtualProfileProvisioner(),
            gameCatalog = AndroidGameCatalog(context),
            onboardingStore = OnboardingStore(context),
            settingsNavigator = AndroidSettingsNavigator(context),
            themeRepository = LocalThemeRepository(context),
            languageRepository = LocalLanguageRepository(context),
            launchPreferencesRepository = LocalLaunchPreferencesRepository(context),
            shortcutPublisher = AndroidWorkspaceShortcutPublisher(context),
            diagnosticReporter = AndroidDiagnosticReporter(context),
            privacyLockRepository = LocalPrivacyLockRepository(context),
        ) as T
    }
}
