package com.duplicateapp.gamespace

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.duplicateapp.gamespace.core.theme.gameSpaceTheme
import com.duplicateapp.gamespace.features.workspace.presentation.workspaceScreen
import com.duplicateapp.gamespace.features.workspace.presentation.WorkspaceViewModel

@Composable
fun gameSpaceApp() {
    val viewModel: WorkspaceViewModel = viewModel(factory = WorkspaceViewModel.Factory(LocalContext.current))

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshProfileStatus()
    }

    gameSpaceTheme {
        workspaceScreen(viewModel = viewModel)
    }
}
