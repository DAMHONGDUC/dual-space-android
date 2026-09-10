package com.duplicateapp.gamespace.features.workspace.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.content.Context
import com.duplicateapp.gamespace.features.workspace.data.VirtualizedGameLauncher
import com.duplicateapp.gamespace.features.workspace.data.AndroidProfileProvisioner
import com.duplicateapp.gamespace.features.workspace.data.AndroidGameCatalog
import com.duplicateapp.gamespace.features.workspace.domain.GameLaunchResult
import com.duplicateapp.gamespace.features.workspace.domain.GameLauncher
import com.duplicateapp.gamespace.features.workspace.domain.LaunchUnavailableReason
import com.duplicateapp.gamespace.features.workspace.domain.ProfileProvisioner
import com.duplicateapp.gamespace.features.workspace.domain.ProfileProvisioningStatus
import com.duplicateapp.gamespace.features.workspace.domain.GameCatalog
import com.duplicateapp.gamespace.features.workspace.domain.InstalledGame
import com.duplicateapp.gamespace.features.workspace.domain.ProfileTarget
import androidx.lifecycle.viewModelScope
import com.duplicateapp.gamespace.features.quota.data.LocalQuotaTracker
import com.duplicateapp.gamespace.features.onboarding.data.OnboardingStore
import com.duplicateapp.gamespace.features.workspace.data.PersistentWorkspaceRepository
import com.duplicateapp.gamespace.features.workspace.domain.GameSession
import com.duplicateapp.gamespace.features.workspace.domain.WorkspaceRepository
import com.duplicateapp.gamespace.features.workspace.domain.SettingsNavigator
import com.duplicateapp.gamespace.features.workspace.domain.SessionState
import com.duplicateapp.gamespace.features.workspace.data.AndroidSettingsNavigator
import com.duplicateapp.gamespace.features.virtualization.data.TheUniverseVirtualGameRuntime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkspaceViewModel(
    private val repository: WorkspaceRepository,
    private val gameLauncher: GameLauncher,
    private val profileProvisioner: ProfileProvisioner,
    private val gameCatalog: GameCatalog,
    private val quotaTracker: LocalQuotaTracker,
    private val onboardingStore: OnboardingStore,
    private val settingsNavigator: SettingsNavigator,
) : ViewModel() {
    private val mutableSelectedSessionId = MutableStateFlow(repository.selectedSessionId.value)
    private val mutableLaunchMessage = MutableStateFlow<Int?>(null)
    private val mutableProfileProvisioningStatus = MutableStateFlow(profileProvisioner.status())
    private val mutableInstalledGames = MutableStateFlow<List<InstalledGame>>(emptyList())
    private val mutableIsAddSessionVisible = MutableStateFlow(false)
    private val mutableRemainingQuotaHours = MutableStateFlow(quotaTracker.remainingHours())
    private val mutableIsOnboardingVisible = MutableStateFlow(!onboardingStore.isComplete())
    private val mutableIsDeleteConfirmationVisible = MutableStateFlow(false)

    val sessions: StateFlow<List<GameSession>> = repository.sessions
    val selectedSessionId: StateFlow<String?> = mutableSelectedSessionId.asStateFlow()
    val launchMessage: StateFlow<Int?> = mutableLaunchMessage.asStateFlow()
    val remainingQuotaHours: StateFlow<Int> = mutableRemainingQuotaHours.asStateFlow()
    val profileProvisioningStatus: StateFlow<ProfileProvisioningStatus> = mutableProfileProvisioningStatus.asStateFlow()
    val installedGames: StateFlow<List<InstalledGame>> = mutableInstalledGames.asStateFlow()
    val isAddSessionVisible: StateFlow<Boolean> = mutableIsAddSessionVisible.asStateFlow()
    val isOnboardingVisible: StateFlow<Boolean> = mutableIsOnboardingVisible.asStateFlow()
    val isDeleteConfirmationVisible: StateFlow<Boolean> = mutableIsDeleteConfirmationVisible.asStateFlow()

    fun selectSession(sessionId: String) {
        mutableSelectedSessionId.value = sessionId
        viewModelScope.launch {
            repository.selectSession(sessionId)
        }
    }

    fun toggleSelectedSession() {
        val session: GameSession = sessions.value.firstOrNull { item -> item.id == mutableSelectedSessionId.value } ?: return
        launch(session)
    }

    fun launchSession(sessionId: String) {
        val session: GameSession = sessions.value.firstOrNull { item -> item.id == sessionId } ?: return
        mutableSelectedSessionId.value = sessionId
        viewModelScope.launch { repository.selectSession(sessionId) }
        launch(session)
    }

    private fun launch(session: GameSession) {
        if (!quotaTracker.canLaunch()) {
            mutableLaunchMessage.value = com.duplicateapp.gamespace.R.string.quota_exhausted
            return
        }
        viewModelScope.launch {
            repository.updateSessionState(session.id, SessionState.starting)
            mutableLaunchMessage.value = when (val result: GameLaunchResult = gameLauncher.launch(session)) {
                is GameLaunchResult.Opened -> {
                    repository.updateSessionState(session.id, SessionState.running)
                    quotaTracker.startSession()
                    com.duplicateapp.gamespace.R.string.game_opened
                }
                is GameLaunchResult.Unavailable -> {
                    repository.updateSessionState(session.id, SessionState.failed)
                    when (result.reason) {
                        LaunchUnavailableReason.missingManagedProfile -> com.duplicateapp.gamespace.R.string.missing_managed_profile
                        LaunchUnavailableReason.gameNotInstalled -> com.duplicateapp.gamespace.R.string.game_not_installed
                        LaunchUnavailableReason.permissionDenied -> com.duplicateapp.gamespace.R.string.profile_permission_denied
                    }
                }
            }
        }
    }

    fun clearLaunchMessage() {
        mutableLaunchMessage.value = null
    }

    fun createProfile() {
        if (!profileProvisioner.requestCreation()) {
            mutableLaunchMessage.value = com.duplicateapp.gamespace.R.string.profile_creation_failed
        }
    }

    fun refreshProfileStatus() {
        quotaTracker.settleSession()
        mutableRemainingQuotaHours.value = quotaTracker.remainingHours()
        mutableProfileProvisioningStatus.value = profileProvisioner.status()
        viewModelScope.launch {
            sessions.value.filter { session -> session.state == SessionState.running }
                .forEach { session -> repository.updateSessionState(session.id, SessionState.paused) }
        }
    }

    fun completeOnboarding() {
        onboardingStore.complete()
        mutableIsOnboardingVisible.value = false
    }

    fun showAddSession(profileTarget: ProfileTarget = ProfileTarget.personal) {
        mutableInstalledGames.value = gameCatalog.listInstalledGames(profileTarget)
        mutableIsAddSessionVisible.value = true
    }

    fun changeCatalogProfile(profileTarget: ProfileTarget) {
        mutableInstalledGames.value = gameCatalog.listInstalledGames(profileTarget)
    }

    fun dismissAddSession() {
        mutableIsAddSessionVisible.value = false
    }

    fun addSession(name: String, game: InstalledGame) {
        val existingCopies: List<GameSession> = sessions.value.filter { session -> session.packageName == game.packageName }
        if (existingCopies.size >= maximumCopiesPerGame) {
            mutableLaunchMessage.value = com.duplicateapp.gamespace.R.string.game_already_added
            return
        }
        val target: ProfileTarget = if (existingCopies.isEmpty()) ProfileTarget.personal else ProfileTarget.managed
        val copyName: String = if (target == ProfileTarget.personal) name else "$name · Copy 1"
        viewModelScope.launch {
            repository.addSession(copyName, game.label, game.packageName, target)
            val addedSessionId: String = repository.sessions.value.last().id
            mutableSelectedSessionId.value = addedSessionId
            repository.selectSession(addedSessionId)
            mutableIsAddSessionVisible.value = false
        }
    }

    fun deleteSelectedSession() {
        val sessionId: String = mutableSelectedSessionId.value ?: return
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            mutableSelectedSessionId.value = repository.sessions.value.firstOrNull()?.id
            mutableIsDeleteConfirmationVisible.value = false
        }
    }

    fun requestDeleteSelectedSession() {
        if (sessions.value.isEmpty()) return
        mutableIsDeleteConfirmationVisible.value = true
    }

    fun requestDeleteSession(sessionId: String) {
        mutableSelectedSessionId.value = sessionId
        viewModelScope.launch { repository.selectSession(sessionId) }
        mutableIsDeleteConfirmationVisible.value = true
    }

    fun dismissDeleteConfirmation() {
        mutableIsDeleteConfirmationVisible.value = false
    }

    fun openAndroidSettings() {
        if (!settingsNavigator.openAndroidSettings()) {
            mutableLaunchMessage.value = com.duplicateapp.gamespace.R.string.settings_open_failed
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WorkspaceViewModel(
                PersistentWorkspaceRepository(context),
                VirtualizedGameLauncher(TheUniverseVirtualGameRuntime()),
                AndroidProfileProvisioner(context),
                AndroidGameCatalog(context),
                LocalQuotaTracker(context),
                OnboardingStore(context),
                AndroidSettingsNavigator(context),
            ) as T
        }
    }

    private companion object {
        const val maximumCopiesPerGame = 2
    }
}
