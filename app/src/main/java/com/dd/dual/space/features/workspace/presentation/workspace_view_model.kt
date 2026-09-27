package com.dd.dual.space.features.workspace.presentation

import androidx.lifecycle.ViewModel
import android.app.Activity
import com.dd.dual.space.features.auth.data.GoogleCredentialProvider
import com.dd.dual.space.features.auth.domain.AuthRepository
import com.dd.dual.space.features.auth.domain.AuthSession
import com.dd.dual.space.features.premium.data.RevenueCatPurchaseManager
import com.dd.dual.space.features.premium.domain.PurchaseOutcome
import com.dd.dual.space.features.premium.domain.PremiumAccess
import com.dd.dual.space.features.premium.domain.PremiumRepository
import com.dd.dual.space.features.premium.domain.PremiumStatus
import com.dd.dual.space.features.workspace.domain.GameLaunchResult
import com.dd.dual.space.features.workspace.domain.GameLauncher
import com.dd.dual.space.features.workspace.domain.GameCopyRemover
import com.dd.dual.space.features.workspace.domain.GameCopyLimits
import com.dd.dual.space.features.workspace.domain.LaunchUnavailableReason
import com.dd.dual.space.features.workspace.domain.ProfileProvisioner
import com.dd.dual.space.features.workspace.domain.ProfileProvisioningStatus
import com.dd.dual.space.features.workspace.domain.GameCatalog
import com.dd.dual.space.features.workspace.domain.InstalledGame
import com.dd.dual.space.features.workspace.domain.ProfileTarget
import androidx.lifecycle.viewModelScope
import com.dd.dual.space.features.onboarding.data.OnboardingStore
import com.dd.dual.space.features.workspace.domain.GameSession
import com.dd.dual.space.features.workspace.domain.WorkspaceRepository
import com.dd.dual.space.features.workspace.domain.SettingsNavigator
import com.dd.dual.space.features.workspace.domain.GameLaunchReadiness
import com.dd.dual.space.features.workspace.domain.WorkspaceShortcutPublisher
import com.dd.dual.space.features.workspace.domain.DiagnosticReporter
import com.dd.dual.space.features.privacy.domain.PrivacyLockRepository
import com.dd.dual.space.features.settings.domain.AppLanguage
import com.dd.dual.space.features.settings.domain.LanguageRepository
import com.dd.dual.space.features.settings.domain.LaunchPreferencesRepository
import com.dd.dual.space.features.settings.domain.ThemeMode
import com.dd.dual.space.features.settings.domain.ThemeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WorkspaceViewModel(
    private val repository: WorkspaceRepository,
    private val gameLauncher: GameLauncher,
    private val gameCopyRemover: GameCopyRemover,
    private val profileProvisioner: ProfileProvisioner,
    private val gameCatalog: GameCatalog,
    private val onboardingStore: OnboardingStore,
    private val settingsNavigator: SettingsNavigator,
    private val themeRepository: ThemeRepository,
    private val languageRepository: LanguageRepository,
    private val launchPreferencesRepository: LaunchPreferencesRepository,
    private val authRepository: AuthRepository,
    private val premiumRepository: PremiumRepository,
    private val googleCredentialProvider: GoogleCredentialProvider,
    private val purchaseManager: RevenueCatPurchaseManager,
    private val shortcutPublisher: WorkspaceShortcutPublisher,
    private val diagnosticReporter: DiagnosticReporter,
    private val privacyLockRepository: PrivacyLockRepository,
) : ViewModel() {
    private val mutableSelectedSessionId = MutableStateFlow(repository.selectedSessionId.value)
    private val mutableLaunchMessage = MutableStateFlow<Int?>(null)
    private val mutableProfileProvisioningStatus = MutableStateFlow(profileProvisioner.status())
    private val mutableInstalledGames = MutableStateFlow<List<InstalledGame>>(emptyList())
    private val mutableIsAddSessionVisible = MutableStateFlow(false)
    private val mutableIsOnboardingVisible = MutableStateFlow(!onboardingStore.isComplete())
    private val mutableIsDeleteConfirmationVisible = MutableStateFlow(false)
    private val mutablePendingDeleteSessionIds = MutableStateFlow<List<String>>(emptyList())
    private val mutableDeleteConfirmationName = MutableStateFlow<String?>(null)
    private val mutableIsDeletingWholeGame = MutableStateFlow(false)
    private val mutableThemeMode = MutableStateFlow(themeRepository.load())
    private val mutableAppLanguage = MutableStateFlow(languageRepository.load())
    private val mutableAuthSession = MutableStateFlow(authRepository.currentSession())
    private val mutablePremiumAccess = MutableStateFlow(PremiumAccess(PremiumStatus.unknown))
    private val mutableIsMonetizationBusy = MutableStateFlow(false)
    private val mutableReadinessBySessionId = MutableStateFlow<Map<String, GameLaunchReadiness>>(emptyMap())
    private val mutablePendingLaunchSession = MutableStateFlow<GameSession?>(null)
    private val mutableConfirmBeforeLaunch = MutableStateFlow(launchPreferencesRepository.confirmBeforeLaunch())
    private val mutableRunningSessionIds = MutableStateFlow<Set<String>>(emptySet())
    private val mutablePrivacyLockEnabled = MutableStateFlow(privacyLockRepository.isEnabled())
    private val mutablePrivacyLockAvailable = MutableStateFlow(privacyLockRepository.isAvailable())
    private val mutableEditingSession = MutableStateFlow<GameSession?>(null)
    private val mutableIsCompatibilityCenterVisible = MutableStateFlow(false)
    private var catalogLoadJob: Job? = null
    private var isAddingSession: Boolean = false
    private var isWorkspaceReady: Boolean = false

    val sessions: StateFlow<List<GameSession>> = repository.sessions
    val selectedSessionId: StateFlow<String?> = mutableSelectedSessionId.asStateFlow()
    val launchMessage: StateFlow<Int?> = mutableLaunchMessage.asStateFlow()
    val profileProvisioningStatus: StateFlow<ProfileProvisioningStatus> = mutableProfileProvisioningStatus.asStateFlow()
    val installedGames: StateFlow<List<InstalledGame>> = mutableInstalledGames.asStateFlow()
    val isAddSessionVisible: StateFlow<Boolean> = mutableIsAddSessionVisible.asStateFlow()
    val isOnboardingVisible: StateFlow<Boolean> = mutableIsOnboardingVisible.asStateFlow()
    val isDeleteConfirmationVisible: StateFlow<Boolean> = mutableIsDeleteConfirmationVisible.asStateFlow()
    val deleteConfirmationName: StateFlow<String?> = mutableDeleteConfirmationName.asStateFlow()
    val isDeletingWholeGame: StateFlow<Boolean> = mutableIsDeletingWholeGame.asStateFlow()
    val themeMode: StateFlow<ThemeMode> = mutableThemeMode.asStateFlow()
    val appLanguage: StateFlow<AppLanguage> = mutableAppLanguage.asStateFlow()
    val authSession: StateFlow<AuthSession?> = mutableAuthSession.asStateFlow()
    val premiumAccess: StateFlow<PremiumAccess> = mutablePremiumAccess.asStateFlow()
    val isMonetizationBusy: StateFlow<Boolean> = mutableIsMonetizationBusy.asStateFlow()
    val readinessBySessionId: StateFlow<Map<String, GameLaunchReadiness>> = mutableReadinessBySessionId.asStateFlow()
    val pendingLaunchSession: StateFlow<GameSession?> = mutablePendingLaunchSession.asStateFlow()
    val confirmBeforeLaunch: StateFlow<Boolean> = mutableConfirmBeforeLaunch.asStateFlow()
    val runningSessionIds: StateFlow<Set<String>> = mutableRunningSessionIds.asStateFlow()
    val privacyLockEnabled: StateFlow<Boolean> = mutablePrivacyLockEnabled.asStateFlow()
    val privacyLockAvailable: StateFlow<Boolean> = mutablePrivacyLockAvailable.asStateFlow()
    val editingSession: StateFlow<GameSession?> = mutableEditingSession.asStateFlow()
    val isCompatibilityCenterVisible: StateFlow<Boolean> = mutableIsCompatibilityCenterVisible.asStateFlow()

    fun setThemeMode(themeMode: ThemeMode) {
        themeRepository.save(themeMode)
        mutableThemeMode.value = themeMode
    }

    fun setAppLanguage(language: AppLanguage) {
        languageRepository.save(language)
        mutableAppLanguage.value = language
    }

    fun selectSession(sessionId: String) {
        mutableSelectedSessionId.value = sessionId
        viewModelScope.launch {
            repository.selectSession(sessionId)
        }
    }

    fun toggleSelectedSession() {
        val session: GameSession = sessions.value.firstOrNull { item -> item.id == mutableSelectedSessionId.value } ?: return
        launchSession(session.id)
    }

    fun launchSession(sessionId: String) {
        val session: GameSession = sessions.value.firstOrNull { item -> item.id == sessionId } ?: return
        mutableSelectedSessionId.value = sessionId
        viewModelScope.launch { repository.selectSession(sessionId) }
        val readiness: GameLaunchReadiness = readinessFor(session)
        if (readiness !is GameLaunchReadiness.Ready || mutableConfirmBeforeLaunch.value) {
            mutablePendingLaunchSession.value = session
        } else {
            launch(session)
        }
    }

    fun setConfirmBeforeLaunch(isEnabled: Boolean) {
        launchPreferencesRepository.setConfirmBeforeLaunch(isEnabled)
        mutableConfirmBeforeLaunch.value = isEnabled
    }

    fun onWorkspaceResumed() {
        if (!isWorkspaceReady) return
        refreshReadiness()
        refreshRunningSessions()
    }

    fun onWorkspaceReady() {
        if (isWorkspaceReady) return
        isWorkspaceReady = true
        refreshReadiness()
        refreshRunningSessions()
        refreshPremiumAccess()
        viewModelScope.launch(Dispatchers.IO) { shortcutPublisher.publish(sessions.value) }
    }

    fun confirmLaunch() {
        val session: GameSession = mutablePendingLaunchSession.value ?: return
        mutablePendingLaunchSession.value = null
        launch(session)
    }

    fun dismissLaunchConfirmation() {
        mutablePendingLaunchSession.value = null
    }

    private fun launch(session: GameSession) {
        viewModelScope.launch {
            val result: GameLaunchResult = withContext(Dispatchers.IO) { gameLauncher.launch(session) }
            mutableLaunchMessage.value = when (result) {
                is GameLaunchResult.Opened -> {
                    repository.recordSessionOpened(session.id, System.currentTimeMillis())
                    shortcutPublisher.publish(repository.sessions.value)
                    mutableRunningSessionIds.value = mutableRunningSessionIds.value + session.id
                    com.dd.dual.space.R.string.game_opened
                }
                is GameLaunchResult.Unavailable -> {
                    mutableReadinessBySessionId.value = mutableReadinessBySessionId.value +
                        (session.id to GameLaunchReadiness.Unavailable(result.reason))
                    when (result.reason) {
                        LaunchUnavailableReason.missingManagedProfile -> com.dd.dual.space.R.string.missing_managed_profile
                        LaunchUnavailableReason.gameNotInstalled -> com.dd.dual.space.R.string.game_not_installed
                        LaunchUnavailableReason.permissionDenied -> com.dd.dual.space.R.string.profile_permission_denied
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
            mutableLaunchMessage.value = com.dd.dual.space.R.string.profile_creation_failed
        }
    }

    fun refreshEnvironment() {
        mutableProfileProvisioningStatus.value = profileProvisioner.status()
        refreshReadiness()
        if (mutableIsAddSessionVisible.value) loadInstalledGames()
    }

    fun completeOnboarding() {
        onboardingStore.complete()
        mutableIsOnboardingVisible.value = false
    }

    fun showAddSession() {
        mutableIsAddSessionVisible.value = true
        loadInstalledGames()
    }

    fun refreshInstalledGames() {
        loadInstalledGames()
    }

    private fun loadInstalledGames() {
        catalogLoadJob?.cancel()
        mutableInstalledGames.value = emptyList()
        catalogLoadJob = viewModelScope.launch {
            mutableInstalledGames.value = withContext(Dispatchers.IO) {
                gameCatalog.listInstalledGames(ProfileTarget.personal)
            }
        }
    }

    fun dismissAddSession() {
        catalogLoadJob?.cancel()
        catalogLoadJob = null
        mutableIsAddSessionVisible.value = false
    }

    fun addSession(name: String, game: InstalledGame) {
        if (!game.isCopyAvailable) {
            mutableLaunchMessage.value = com.dd.dual.space.R.string.copy_install_required
            return
        }
        if (isAddingSession) return
        isAddingSession = true
        viewModelScope.launch {
            try {
                val existingCopies: List<GameSession> = sessions.value.filter { session -> session.packageName == game.packageName }
                val virtualUserId: Int = GameCopyLimits.nextAvailableVirtualUserId(
                    existingCopies.map(GameSession::virtualUserId).toSet(),
                ) ?: run {
                    mutableLaunchMessage.value = com.dd.dual.space.R.string.game_already_added
                    return@launch
                }
                repository.addSession(name, game.label, game.packageName, ProfileTarget.managed, virtualUserId)
                val addedSessionId: String = repository.sessions.value.last().id
                mutableSelectedSessionId.value = addedSessionId
                repository.selectSession(addedSessionId)
                shortcutPublisher.publish(repository.sessions.value)
                refreshReadiness()
                mutableIsAddSessionVisible.value = false
            } finally {
                isAddingSession = false
            }
        }
    }

    fun deleteSelectedSession() {
        val sessionIds: List<String> = mutablePendingDeleteSessionIds.value.ifEmpty {
            listOfNotNull(mutableSelectedSessionId.value)
        }
        if (sessionIds.isEmpty()) return
        viewModelScope.launch {
            val sessionsToDelete: List<GameSession> = sessions.value.filter { session -> session.id in sessionIds }
            val removalResults: Map<GameSession, Boolean> = withContext(Dispatchers.IO) {
                sessionsToDelete.associateWith(gameCopyRemover::remove)
            }
            removalResults.filterValues { removed -> removed }.keys.forEach { session ->
                repository.deleteSession(session.id)
            }
            if (removalResults.any { (_, removed) -> !removed }) {
                mutableLaunchMessage.value = com.dd.dual.space.R.string.copy_removal_failed
            }
            shortcutPublisher.publish(repository.sessions.value)
            refreshReadiness()
            mutableSelectedSessionId.value = repository.sessions.value.firstOrNull()?.id
            mutablePendingDeleteSessionIds.value = emptyList()
            mutableDeleteConfirmationName.value = null
            mutableIsDeletingWholeGame.value = false
            mutableIsDeleteConfirmationVisible.value = false
        }
    }

    fun requestDeleteSelectedSession() {
        val session: GameSession = sessions.value.firstOrNull { it.id == mutableSelectedSessionId.value } ?: return
        mutablePendingDeleteSessionIds.value = listOf(session.id)
        mutableDeleteConfirmationName.value = session.gameName
        mutableIsDeletingWholeGame.value = false
        mutableIsDeleteConfirmationVisible.value = true
    }

    fun requestDeleteSession(sessionId: String) {
        val session: GameSession = sessions.value.firstOrNull { it.id == sessionId } ?: return
        mutableSelectedSessionId.value = sessionId
        mutablePendingDeleteSessionIds.value = listOf(sessionId)
        mutableDeleteConfirmationName.value = session.name
        mutableIsDeletingWholeGame.value = false
        viewModelScope.launch { repository.selectSession(sessionId) }
        mutableIsDeleteConfirmationVisible.value = true
    }

    fun requestDeleteGame(packageName: String) {
        val gameSessions: List<GameSession> = sessions.value.filter { session -> session.packageName == packageName }
        if (gameSessions.isEmpty()) return
        mutablePendingDeleteSessionIds.value = gameSessions.map(GameSession::id)
        mutableDeleteConfirmationName.value = gameSessions.first().gameName
        mutableIsDeletingWholeGame.value = true
        mutableIsDeleteConfirmationVisible.value = true
    }

    fun dismissDeleteConfirmation() {
        mutablePendingDeleteSessionIds.value = emptyList()
        mutableDeleteConfirmationName.value = null
        mutableIsDeletingWholeGame.value = false
        mutableIsDeleteConfirmationVisible.value = false
    }

    fun openAndroidSettings() {
        if (!settingsNavigator.openAndroidSettings()) {
            mutableLaunchMessage.value = com.dd.dual.space.R.string.settings_open_failed
        }
    }

    fun setPrivacyLockEnabled(isEnabled: Boolean) {
        if (isEnabled && !privacyLockRepository.isAvailable()) {
            mutableLaunchMessage.value = com.dd.dual.space.R.string.privacy_lock_unavailable
            return
        }
        privacyLockRepository.setEnabled(isEnabled)
        mutablePrivacyLockEnabled.value = isEnabled
    }

    fun shareDiagnosticReport() {
        val session: GameSession? = sessions.value.firstOrNull { item -> item.id == mutableSelectedSessionId.value }
        diagnosticReporter.share(
            profileTarget = session?.profileTarget ?: ProfileTarget.managed,
            readiness = session?.let { item -> mutableReadinessBySessionId.value[item.id] },
        )
    }

    fun showCompatibilityCenter() {
        refreshReadiness()
        mutableIsCompatibilityCenterVisible.value = true
    }

    fun dismissCompatibilityCenter() {
        mutableIsCompatibilityCenterVisible.value = false
    }

    fun editSession(sessionId: String) {
        mutableEditingSession.value = sessions.value.firstOrNull { session -> session.id == sessionId }
    }

    fun dismissSessionEditor() {
        mutableEditingSession.value = null
    }

    fun saveSessionIdentity(name: String, accountColor: com.dd.dual.space.features.workspace.domain.AccountColor) {
        val session: GameSession = mutableEditingSession.value ?: return
        viewModelScope.launch {
            repository.updateSessionIdentity(session.id, name, accountColor)
            shortcutPublisher.publish(repository.sessions.value)
            mutableEditingSession.value = null
        }
    }

    private fun readinessFor(session: GameSession): GameLaunchReadiness {
        val cached: GameLaunchReadiness? = mutableReadinessBySessionId.value[session.id]
        if (cached != null) return cached
        return gameLauncher.readiness(session).also { readiness ->
            mutableReadinessBySessionId.value = mutableReadinessBySessionId.value + (session.id to readiness)
        }
    }

    private fun refreshReadiness() {
        viewModelScope.launch {
            mutableReadinessBySessionId.value = withContext(Dispatchers.IO) {
                sessions.value.associate { session -> session.id to gameLauncher.readiness(session) }
            }
        }
    }

    private fun refreshRunningSessions() {
        viewModelScope.launch {
            mutableRunningSessionIds.value = withContext(Dispatchers.IO) {
                sessions.value.filter(gameLauncher::isRunning).map(GameSession::id).toSet()
            }
        }
    }

    fun signIn(activity: Activity) {
        if (mutableIsMonetizationBusy.value) return
        mutableIsMonetizationBusy.value = true
        viewModelScope.launch {
            try {
                val token: String? = googleCredentialProvider.getIdToken(activity)
                val session: AuthSession? = token?.let { authRepository.authenticateGoogleIdToken(it) }
                mutableAuthSession.value = session
                if (session == null) {
                    mutableLaunchMessage.value = com.dd.dual.space.R.string.sign_in_failed
                } else {
                    mutablePremiumAccess.value = premiumRepository.identify(session.userId)
                }
            } finally {
                mutableIsMonetizationBusy.value = false
            }
        }
    }

    fun signOut() {
        if (mutableIsMonetizationBusy.value) return
        mutableIsMonetizationBusy.value = true
        viewModelScope.launch {
            try {
                premiumRepository.logOut()
                authRepository.signOut()
                mutableAuthSession.value = null
                mutablePremiumAccess.value = PremiumAccess(PremiumStatus.inactive)
            } finally {
                mutableIsMonetizationBusy.value = false
            }
        }
    }

    fun restorePremium() {
        if (mutableIsMonetizationBusy.value) return
        mutableIsMonetizationBusy.value = true
        viewModelScope.launch {
            try {
                val access: PremiumAccess = premiumRepository.restore()
                mutablePremiumAccess.value = access
                mutableLaunchMessage.value = when (access.status) {
                    PremiumStatus.active -> com.dd.dual.space.R.string.premium_restored
                    PremiumStatus.inactive -> com.dd.dual.space.R.string.premium_not_found
                    PremiumStatus.unknown -> com.dd.dual.space.R.string.premium_purchase_failed
                }
            } finally {
                mutableIsMonetizationBusy.value = false
            }
        }
    }

    fun purchasePremium(activity: Activity) {
        if (mutableIsMonetizationBusy.value) return
        mutableIsMonetizationBusy.value = true
        purchaseManager.purchase(activity) { outcome ->
            when (outcome) {
                is PurchaseOutcome.Activated -> {
                    mutablePremiumAccess.value = outcome.access
                    mutableLaunchMessage.value = com.dd.dual.space.R.string.premium_activated
                }
                is PurchaseOutcome.NotActivated -> {
                    mutablePremiumAccess.value = outcome.access
                    mutableLaunchMessage.value = com.dd.dual.space.R.string.premium_purchase_not_active
                }
                PurchaseOutcome.Pending -> mutableLaunchMessage.value = com.dd.dual.space.R.string.premium_purchase_pending
                PurchaseOutcome.Cancelled -> Unit
                PurchaseOutcome.Failed -> mutableLaunchMessage.value = com.dd.dual.space.R.string.premium_purchase_failed
            }
            mutableIsMonetizationBusy.value = false
        }
    }

    private fun refreshPremiumAccess() {
        viewModelScope.launch {
            mutablePremiumAccess.value = mutableAuthSession.value?.let { premiumRepository.identify(it.userId) }
                ?: premiumRepository.refresh()
        }
    }

}
