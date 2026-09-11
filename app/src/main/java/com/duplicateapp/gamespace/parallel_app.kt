package com.duplicateapp.gamespace

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.duplicateapp.gamespace.core.theme.parallelAppTheme
import com.duplicateapp.gamespace.features.workspace.presentation.workspaceScreen
import com.duplicateapp.gamespace.features.workspace.presentation.WorkspaceViewModel

@Composable
fun parallelAppApp() {
    val viewModel: WorkspaceViewModel = viewModel(factory = WorkspaceViewModel.Factory(LocalContext.current))
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshProfileStatus()
    }

    parallelAppTheme(themeMode) {
        workspaceScreen(viewModel = viewModel)
    }
}
