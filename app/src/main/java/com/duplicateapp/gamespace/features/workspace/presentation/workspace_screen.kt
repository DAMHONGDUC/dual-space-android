package com.duplicateapp.gamespace.features.workspace.presentation

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import androidx.activity.compose.LocalActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.duplicateapp.gamespace.features.onboarding.presentation.onboardingDialog
import com.duplicateapp.gamespace.BuildConfig
import com.duplicateapp.gamespace.features.ads.presentation.bannerAd
import com.duplicateapp.gamespace.features.workspace.domain.*
import com.duplicateapp.gamespace.features.workspace.presentation.components.*

@Composable
fun workspaceScreen(viewModel: WorkspaceViewModel) {
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val launchMessageResId by viewModel.launchMessage.collectAsStateWithLifecycle()
    val profileStatus by viewModel.profileProvisioningStatus.collectAsStateWithLifecycle()
    val installedGames by viewModel.installedGames.collectAsStateWithLifecycle()
    val isAddVisible by viewModel.isAddSessionVisible.collectAsStateWithLifecycle()
    val isOnboardingVisible by viewModel.isOnboardingVisible.collectAsStateWithLifecycle()
    val isDeleteVisible by viewModel.isDeleteConfirmationVisible.collectAsStateWithLifecycle()
    val deleteConfirmationName by viewModel.deleteConfirmationName.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val authSession by viewModel.authSession.collectAsStateWithLifecycle()
    val premiumAccess by viewModel.premiumAccess.collectAsStateWithLifecycle()
    val isMonetizationBusy by viewModel.isMonetizationBusy.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    val snackbar = remember { SnackbarHostState() }
    val launchMessage = launchMessageResId?.let { stringResource(it) }
    var isSettingsVisible by rememberSaveable { mutableStateOf(false) }

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
            onProfileChange = viewModel::changeCatalogProfile,
            onAdd = viewModel::addSession,
        )
    }
    if (isOnboardingVisible) onboardingDialog(viewModel::completeOnboarding)
    if (isSettingsVisible) {
        settingsDialog(
            profileStatus = profileStatus,
            themeMode = themeMode,
            appLanguage = appLanguage,
            authSession = authSession,
            premiumAccess = premiumAccess,
            isMonetizationBusy = isMonetizationBusy,
            onThemeModeChange = viewModel::setThemeMode,
            onLanguageChange = { language ->
                viewModel.setAppLanguage(language)
                activity?.recreate()
            },
            onOpenAndroidSettings = viewModel::openAndroidSettings,
            onSignIn = { activity?.let(viewModel::signIn) },
            onSignOut = viewModel::signOut,
            onPurchasePremium = { activity?.let(viewModel::purchasePremium) },
            onRestorePremium = viewModel::restorePremium,
            onDismiss = { isSettingsVisible = false },
        )
    }
    if (isDeleteVisible && deleteConfirmationName != null) {
        deleteGameDialog(deleteConfirmationName.orEmpty(), viewModel::deleteSelectedSession, viewModel::dismissDeleteConfirmation)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (premiumAccess.showsAds) bannerAd(BuildConfig.ADMOB_BANNER_AD_UNIT_ID)
        },
    ) { padding ->
        gameLibrary(
            sessions = sessions,
            profileStatus = profileStatus,
            contentPadding = padding,
            onLaunch = viewModel::launchSession,
            onAdd = { viewModel.showAddSession() },
            onDeleteGame = viewModel::requestDeleteGame,
            onCreateProfile = viewModel::createProfile,
            onOpenAndroidSettings = viewModel::openAndroidSettings,
            onSettings = { isSettingsVisible = true },
        )
    }
}
