package com.duplicateapp.gamespace.features.workspace.presentation

import androidx.lifecycle.ViewModel
import android.app.Activity
import com.duplicateapp.gamespace.features.auth.data.GoogleCredentialProvider
import com.duplicateapp.gamespace.features.auth.domain.AuthRepository
import com.duplicateapp.gamespace.features.auth.domain.AuthSession
import com.duplicateapp.gamespace.features.premium.data.RevenueCatPurchaseManager
import com.duplicateapp.gamespace.features.premium.domain.PremiumAccess
import com.duplicateapp.gamespace.features.premium.domain.PremiumRepository
import com.duplicateapp.gamespace.features.premium.domain.PremiumStatus
import com.duplicateapp.gamespace.features.workspace.domain.GameLaunchResult
import com.duplicateapp.gamespace.features.workspace.domain.GameLauncher
import com.duplicateapp.gamespace.features.workspace.domain.LaunchUnavailableReason
import com.duplicateapp.gamespace.features.workspace.domain.ProfileProvisioner
import com.duplicateapp.gamespace.features.workspace.domain.ProfileProvisioningStatus
import com.duplicateapp.gamespace.features.workspace.domain.GameCatalog
import com.duplicateapp.gamespace.features.workspace.domain.InstalledGame
import com.duplicateapp.gamespace.features.workspace.domain.ProfileTarget
import androidx.lifecycle.viewModelScope
import com.duplicateapp.gamespace.features.onboarding.data.OnboardingStore
import com.duplicateapp.gamespace.features.workspace.domain.GameSession
import com.duplicateapp.gamespace.features.workspace.domain.WorkspaceRepository
import com.duplicateapp.gamespace.features.workspace.domain.SettingsNavigator
import com.duplicateapp.gamespace.features.workspace.domain.GameLaunchReadiness
import com.duplicateapp.gamespace.features.workspace.domain.WorkspaceShortcutPublisher
import com.duplicateapp.gamespace.features.workspace.domain.DiagnosticReporter
import com.duplicateapp.gamespace.features.privacy.domain.PrivacyLockRepository
import com.duplicateapp.gamespace.features.settings.domain.AppLanguage
import com.duplicateapp.gamespace.features.settings.domain.LanguageRepository
import com.duplicateapp.gamespace.features.settings.domain.ThemeMode
import com.duplicateapp.gamespace.features.settings.domain.ThemeRepository
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
    private val profileProvisioner: ProfileProvisioner,
    private val gameCatalog: GameCatalog,
    private val onboardingStore: OnboardingStore,
    private val settingsNavigator: SettingsNavigator,
    private val themeRepository: ThemeRepository,
    private val languageRepository: LanguageRepository,
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
    private val mutableThemeMode = MutableStateFlow(themeRepository.load())
    private val mutableAppLanguage = MutableStateFlow(languageRepository.load())
    private val mutableAuthSession = MutableStateFlow(authRepository.currentSession())
    private val mutablePremiumAccess = MutableStateFlow(PremiumAccess(PremiumStatus.unknown))
    private val mutableIsMonetizationBusy = MutableStateFlow(false)
    private val mutableReadinessBySessionId = MutableStateFlow<Map<String, GameLaunchReadiness>>(emptyMap())
    private val mutablePendingLaunchSession = MutableStateFlow<GameSession?>(null)
    private val mutablePrivacyLockEnabled = MutableStateFlow(privacyLockRepository.isEnabled())
    private val mutablePrivacyLockAvailable = MutableStateFlow(privacyLockRepository.isAvailable())
    private var catalogLoadJob: Job? = null

    val sessions: StateFlow<List<GameSession>> = repository.sessions
    val selectedSessionId: StateFlow<String?> = mutableSelectedSessionId.asStateFlow()
    val launchMessage: StateFlow<Int?> = mutableLaunchMessage.asStateFlow()
    val profileProvisioningStatus: StateFlow<ProfileProvisioningStatus> = mutableProfileProvisioningStatus.asStateFlow()
    val installedGames: StateFlow<List<InstalledGame>> = mutableInstalledGames.asStateFlow()
    val isAddSessionVisible: StateFlow<Boolean> = mutableIsAddSessionVisible.asStateFlow()
    val isOnboardingVisible: StateFlow<Boolean> = mutableIsOnboardingVisible.asStateFlow()
    val isDeleteConfirmationVisible: StateFlow<Boolean> = mutableIsDeleteConfirmationVisible.asStateFlow()
    val deleteConfirmationName: StateFlow<String?> = mutableDeleteConfirmationName.asStateFlow()
    val themeMode: StateFlow<ThemeMode> = mutableThemeMode.asStateFlow()
    val appLanguage: StateFlow<AppLanguage> = mutableAppLanguage.asStateFlow()
    val authSession: StateFlow<AuthSession?> = mutableAuthSession.asStateFlow()
    val premiumAccess: StateFlow<PremiumAccess> = mutablePremiumAccess.asStateFlow()
    val isMonetizationBusy: StateFlow<Boolean> = mutableIsMonetizationBusy.asStateFlow()
    val readinessBySessionId: StateFlow<Map<String, GameLaunchReadiness>> = mutableReadinessBySessionId.asStateFlow()
    val pendingLaunchSession: StateFlow<GameSession?> = mutablePendingLaunchSession.asStateFlow()
    val privacyLockEnabled: StateFlow<Boolean> = mutablePrivacyLockEnabled.asStateFlow()
    val privacyLockAvailable: StateFlow<Boolean> = mutablePrivacyLockAvailable.asStateFlow()

    init {
        refreshPremiumAccess()
        shortcutPublisher.publish(sessions.value)
        refreshReadiness()
    }

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
        mutablePendingLaunchSession.value = session
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
            mutableLaunchMessage.value = when (val result: GameLaunchResult = gameLauncher.launch(session)) {
                is GameLaunchResult.Opened -> {
                    repository.recordSessionOpened(session.id, System.currentTimeMillis())
                    shortcutPublisher.publish(repository.sessions.value)
                    com.duplicateapp.gamespace.R.string.game_opened
                }
                is GameLaunchResult.Unavailable -> {
                    mutableReadinessBySessionId.value = mutableReadinessBySessionId.value +
                        (session.id to GameLaunchReadiness.Unavailable(result.reason))
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
            mutableLaunchMessage.value = com.duplicateapp.gamespace.R.string.copy_install_required
            return
        }
        val existingCopies: List<GameSession> = sessions.value.filter { session -> session.packageName == game.packageName }
        val profileAlreadyAdded: Boolean = existingCopies.any { session -> session.profileTarget == ProfileTarget.managed }
        if (existingCopies.size >= maximumCopiesPerGame || profileAlreadyAdded) {
            mutableLaunchMessage.value = com.duplicateapp.gamespace.R.string.game_already_added
            return
        }
        viewModelScope.launch {
            repository.addSession(name, game.label, game.packageName, ProfileTarget.managed)
            val addedSessionId: String = repository.sessions.value.last().id
            mutableSelectedSessionId.value = addedSessionId
            repository.selectSession(addedSessionId)
            shortcutPublisher.publish(repository.sessions.value)
            refreshReadiness()
            mutableIsAddSessionVisible.value = false
        }
    }

    fun deleteSelectedSession() {
        val sessionIds: List<String> = mutablePendingDeleteSessionIds.value.ifEmpty {
            listOfNotNull(mutableSelectedSessionId.value)
        }
        if (sessionIds.isEmpty()) return
        viewModelScope.launch {
            sessionIds.forEach { sessionId -> repository.deleteSession(sessionId) }
            shortcutPublisher.publish(repository.sessions.value)
            refreshReadiness()
            mutableSelectedSessionId.value = repository.sessions.value.firstOrNull()?.id
            mutablePendingDeleteSessionIds.value = emptyList()
            mutableDeleteConfirmationName.value = null
            mutableIsDeleteConfirmationVisible.value = false
        }
    }

    fun requestDeleteSelectedSession() {
        val session: GameSession = sessions.value.firstOrNull { it.id == mutableSelectedSessionId.value } ?: return
        mutablePendingDeleteSessionIds.value = listOf(session.id)
        mutableDeleteConfirmationName.value = session.gameName
        mutableIsDeleteConfirmationVisible.value = true
    }

    fun requestDeleteSession(sessionId: String) {
        val session: GameSession = sessions.value.firstOrNull { it.id == sessionId } ?: return
        mutableSelectedSessionId.value = sessionId
        mutablePendingDeleteSessionIds.value = listOf(sessionId)
        mutableDeleteConfirmationName.value = session.gameName
        viewModelScope.launch { repository.selectSession(sessionId) }
        mutableIsDeleteConfirmationVisible.value = true
    }

    fun requestDeleteGame(packageName: String) {
        val gameSessions: List<GameSession> = sessions.value.filter { session -> session.packageName == packageName }
        if (gameSessions.isEmpty()) return
        mutablePendingDeleteSessionIds.value = gameSessions.map(GameSession::id)
        mutableDeleteConfirmationName.value = gameSessions.first().gameName
        mutableIsDeleteConfirmationVisible.value = true
    }

    fun dismissDeleteConfirmation() {
        mutablePendingDeleteSessionIds.value = emptyList()
        mutableDeleteConfirmationName.value = null
        mutableIsDeleteConfirmationVisible.value = false
    }

    fun openAndroidSettings() {
        if (!settingsNavigator.openAndroidSettings()) {
            mutableLaunchMessage.value = com.duplicateapp.gamespace.R.string.settings_open_failed
        }
    }

    fun setPrivacyLockEnabled(isEnabled: Boolean) {
        if (isEnabled && !privacyLockRepository.isAvailable()) {
            mutableLaunchMessage.value = com.duplicateapp.gamespace.R.string.privacy_lock_unavailable
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

    private fun refreshReadiness() {
        mutableReadinessBySessionId.value = sessions.value.associate { session -> session.id to gameLauncher.readiness(session) }
    }

    fun signIn(activity: Activity) {
        if (mutableIsMonetizationBusy.value) return
        mutableIsMonetizationBusy.value = true
        viewModelScope.launch {
            val token: String? = googleCredentialProvider.getIdToken(activity)
            val session: AuthSession? = token?.let { authRepository.authenticateGoogleIdToken(it) }
            mutableAuthSession.value = session
            if (session == null) {
                mutableLaunchMessage.value = com.duplicateapp.gamespace.R.string.sign_in_failed
            } else {
                mutablePremiumAccess.value = premiumRepository.identify(session.userId)
            }
            mutableIsMonetizationBusy.value = false
        }
    }

    fun signOut() {
        if (mutableIsMonetizationBusy.value) return
        mutableIsMonetizationBusy.value = true
        viewModelScope.launch {
            premiumRepository.logOut()
            authRepository.signOut()
            mutableAuthSession.value = null
            mutablePremiumAccess.value = PremiumAccess(PremiumStatus.inactive)
            mutableIsMonetizationBusy.value = false
        }
    }

    fun restorePremium() {
        if (mutableIsMonetizationBusy.value) return
        mutableIsMonetizationBusy.value = true
        viewModelScope.launch {
            mutablePremiumAccess.value = premiumRepository.restore()
            mutableLaunchMessage.value = if (mutablePremiumAccess.value.removesAds) {
                com.duplicateapp.gamespace.R.string.premium_restored
            } else {
                com.duplicateapp.gamespace.R.string.premium_not_found
            }
            mutableIsMonetizationBusy.value = false
        }
    }

    fun purchasePremium(activity: Activity) {
        if (mutableIsMonetizationBusy.value) return
        mutableIsMonetizationBusy.value = true
        purchaseManager.purchase(
            activity = activity,
            onResult = { access ->
                mutablePremiumAccess.value = access
                mutableLaunchMessage.value = com.duplicateapp.gamespace.R.string.premium_activated
                mutableIsMonetizationBusy.value = false
            },
            onUnavailable = {
                mutableLaunchMessage.value = com.duplicateapp.gamespace.R.string.premium_purchase_failed
                mutableIsMonetizationBusy.value = false
            },
        )
    }

    private fun refreshPremiumAccess() {
        viewModelScope.launch {
            mutablePremiumAccess.value = mutableAuthSession.value?.let { premiumRepository.identify(it.userId) }
                ?: premiumRepository.refresh()
        }
    }

    private companion object {
        const val maximumCopiesPerGame = 2
    }
}
