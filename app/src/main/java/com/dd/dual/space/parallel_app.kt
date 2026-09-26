package com.dd.dual.space

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dd.dual.space.core.theme.parallelAppTheme
import com.dd.dual.space.features.workspace.presentation.workspaceScreen
import com.dd.dual.space.features.workspace.presentation.WorkspaceViewModel
import com.dd.dual.space.features.workspace.presentation.WorkspaceViewModelFactory
import com.dd.dual.space.features.update.presentation.forceUpdateDialog
import com.dd.dual.space.features.privacy.presentation.privacyLockScreen
import androidx.compose.runtime.LaunchedEffect

@Composable
fun parallelAppApp(
    canRequestAds: Boolean,
    privacyOptionsRequired: Boolean,
    isForceUpdateRequired: Boolean,
    onOpenPrivacyOptions: () -> Unit,
    onUpdate: () -> Unit,
    onCloseForUpdate: () -> Unit,
    isPrivacyLocked: Boolean,
    shortcutSessionId: String?,
    onShortcutConsumed: () -> Unit,
    onUnlock: () -> Unit,
) {
    val viewModel: WorkspaceViewModel = viewModel(factory = WorkspaceViewModelFactory(LocalContext.current))
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    LaunchedEffect(shortcutSessionId, isPrivacyLocked) {
        if (!isPrivacyLocked) shortcutSessionId?.let { sessionId ->
            viewModel.launchSession(sessionId)
            onShortcutConsumed()
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshEnvironment()
    }

    parallelAppTheme(themeMode) {
        if (isPrivacyLocked) {
            privacyLockScreen(onUnlock)
        } else {
            workspaceScreen(viewModel, canRequestAds, privacyOptionsRequired, onOpenPrivacyOptions)
            if (isForceUpdateRequired) forceUpdateDialog(onUpdate, onCloseForUpdate)
        }
    }
}
