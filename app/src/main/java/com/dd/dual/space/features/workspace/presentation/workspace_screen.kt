package com.dd.dual.space.features.workspace.presentation

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.res.stringResource
import androidx.activity.compose.LocalActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.dd.dual.space.features.onboarding.presentation.onboardingDialog
import com.dd.dual.space.BuildConfig
import com.dd.dual.space.features.ads.presentation.bannerAd
import com.dd.dual.space.features.workspace.domain.*
import com.dd.dual.space.features.workspace.presentation.components.*

@Composable
fun workspaceScreen(
    viewModel: WorkspaceViewModel,
    canRequestAds: Boolean,
    privacyOptionsRequired: Boolean,
    onOpenPrivacyOptions: () -> Unit,
) {
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val launchMessageResId by viewModel.launchMessage.collectAsStateWithLifecycle()
    val profileStatus by viewModel.profileProvisioningStatus.collectAsStateWithLifecycle()
    val installedGames by viewModel.installedGames.collectAsStateWithLifecycle()
    val isAddVisible by viewModel.isAddSessionVisible.collectAsStateWithLifecycle()
    val isOnboardingVisible by viewModel.isOnboardingVisible.collectAsStateWithLifecycle()
    val isDeleteVisible by viewModel.isDeleteConfirmationVisible.collectAsStateWithLifecycle()
    val deleteConfirmationName by viewModel.deleteConfirmationName.collectAsStateWithLifecycle()
    val isDeletingWholeGame by viewModel.isDeletingWholeGame.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val readinessBySessionId by viewModel.readinessBySessionId.collectAsStateWithLifecycle()
    val pendingLaunchSession by viewModel.pendingLaunchSession.collectAsStateWithLifecycle()
    val confirmBeforeLaunch by viewModel.confirmBeforeLaunch.collectAsStateWithLifecycle()
    val runningSessionIds by viewModel.runningSessionIds.collectAsStateWithLifecycle()
    val privacyLockEnabled by viewModel.privacyLockEnabled.collectAsStateWithLifecycle()
    val privacyLockAvailable by viewModel.privacyLockAvailable.collectAsStateWithLifecycle()
    val editingSession by viewModel.editingSession.collectAsStateWithLifecycle()
    val isCompatibilityCenterVisible by viewModel.isCompatibilityCenterVisible.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    val snackbar = remember { SnackbarHostState() }
    val launchMessage = launchMessageResId?.let { stringResource(it) }
    var isSettingsVisible by rememberSaveable { mutableStateOf(false) }
    var isAboutVisible by rememberSaveable { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onWorkspaceResumed() }
    LaunchedEffect(Unit) {
        withFrameNanos { }
        viewModel.onWorkspaceReady()
    }

    LaunchedEffect(launchMessage) {
        if (launchMessage != null) {
            snackbar.showSnackbar(launchMessage)
            viewModel.clearLaunchMessage()
        }
    }
    if (isAddVisible) {
        addSessionDialog(
            games = installedGames,
            onDismiss = viewModel::dismissAddSession,
            onAdd = viewModel::addSession,
            onRefresh = viewModel::refreshInstalledGames,
            onOpenSettings = viewModel::openAndroidSettings,
        )
    }
    pendingLaunchSession?.let { session ->
        launchConfirmationDialog(
            session = session,
            readiness = readinessBySessionId[session.id],
            onConfirm = viewModel::confirmLaunch,
            onDismiss = viewModel::dismissLaunchConfirmation,
            onOpenSettings = {
                viewModel.dismissLaunchConfirmation()
                viewModel.openAndroidSettings()
            },
        )
    }
    editingSession?.let { session ->
        accountIdentityDialog(
            session = session,
            onSave = viewModel::saveSessionIdentity,
            onDelete = {
                viewModel.dismissSessionEditor()
                viewModel.requestDeleteSession(session.id)
            },
            onDismiss = viewModel::dismissSessionEditor,
        )
    }
    if (isCompatibilityCenterVisible) {
        compatibilityCenterDialog(
            sessions = sessions,
            readinessBySessionId = readinessBySessionId,
            onShareReport = viewModel::shareDiagnosticReport,
            onOpenSettings = viewModel::openAndroidSettings,
            onDismiss = viewModel::dismissCompatibilityCenter,
        )
    }
    if (isOnboardingVisible) onboardingDialog(viewModel::completeOnboarding)
    if (isAboutVisible) aboutDialog(onDismiss = { isAboutVisible = false })
    if (isSettingsVisible) {
        settingsDialog(
            profileStatus = profileStatus,
            themeMode = themeMode,
            appLanguage = appLanguage,
            privacyOptionsRequired = privacyOptionsRequired,
            privacyLockEnabled = privacyLockEnabled,
            privacyLockAvailable = privacyLockAvailable,
            confirmBeforeLaunch = confirmBeforeLaunch,
            onThemeModeChange = viewModel::setThemeMode,
            onLanguageChange = { language ->
                viewModel.setAppLanguage(language)
                activity?.recreate()
            },
            onOpenAndroidSettings = viewModel::openAndroidSettings,
            onOpenPrivacyOptions = onOpenPrivacyOptions,
            onPrivacyLockChange = viewModel::setPrivacyLockEnabled,
            onConfirmBeforeLaunchChange = viewModel::setConfirmBeforeLaunch,
            onShareDiagnosticReport = viewModel::shareDiagnosticReport,
            onOpenCompatibilityCenter = viewModel::showCompatibilityCenter,
            onDismiss = { isSettingsVisible = false },
        )
    }
    if (isDeleteVisible && deleteConfirmationName != null) {
        if (isDeletingWholeGame) {
            deleteGameDialog(deleteConfirmationName.orEmpty(), viewModel::deleteSelectedSession, viewModel::dismissDeleteConfirmation)
        } else {
            deleteSessionDialog(deleteConfirmationName.orEmpty(), viewModel::deleteSelectedSession, viewModel::dismissDeleteConfirmation)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (canRequestAds) bannerAd(BuildConfig.ADMOB_BANNER_AD_UNIT_ID)
        },
    ) { padding ->
        gameLibrary(
            sessions = sessions,
            profileStatus = profileStatus,
            readinessBySessionId = readinessBySessionId,
            runningSessionIds = runningSessionIds,
            contentPadding = padding,
            onLaunch = viewModel::launchSession,
            onAdd = { viewModel.showAddSession() },
            onDeleteGame = viewModel::requestDeleteGame,
            onEditSession = viewModel::editSession,
            onCreateProfile = viewModel::createProfile,
            onOpenAndroidSettings = viewModel::openAndroidSettings,
            onHelp = { isAboutVisible = true },
            onSettings = { isSettingsVisible = true },
            isDevEnvironment = BuildConfig.IS_DEV_ENVIRONMENT,
        )
    }
}
