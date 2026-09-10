package com.duplicateapp.gamespace

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.duplicateapp.gamespace.core.theme.game_space_theme
import com.duplicateapp.gamespace.features.workspace.presentation.workspace_screen
import com.duplicateapp.gamespace.features.workspace.presentation.workspace_view_model

@Composable
fun game_space_app() {
    val view_model: workspace_view_model = viewModel(factory = workspace_view_model.factory(LocalContext.current))

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        view_model.refresh_profile_status()
    }

    game_space_theme {
        workspace_screen(view_model = view_model)
    }
}
