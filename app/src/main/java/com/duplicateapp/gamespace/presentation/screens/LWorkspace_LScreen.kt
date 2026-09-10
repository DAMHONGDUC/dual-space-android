package com.duplicateapp.gamespace.features.workspace.presentation

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.duplicateapp.gamespace.features.onboarding.presentation.onboarding_dialog
import com.duplicateapp.gamespace.features.workspace.domain.*
import com.duplicateapp.gamespace.features.workspace.presentation.components.*

@Composable
fun workspace_screen(view_model: workspace_view_model) {
    val sessions by view_model.sessions.collectAsStateWithLifecycle()
    val selected_session_id by view_model.selected_session_id.collectAsStateWithLifecycle()
    val launch_message_res_id by view_model.launch_message.collectAsStateWithLifecycle()
    val profile_status by view_model.profile_provisioning_status.collectAsStateWithLifecycle()
    val installed_games by view_model.installed_games.collectAsStateWithLifecycle()
    val is_add_visible by view_model.is_add_session_visible.collectAsStateWithLifecycle()
    val remaining_hours by view_model.remaining_quota_hours.collectAsStateWithLifecycle()
    val is_onboarding_visible by view_model.is_onboarding_visible.collectAsStateWithLifecycle()
    val is_delete_visible by view_model.is_delete_confirmation_visible.collectAsStateWithLifecycle()
    val selected_session = sessions.firstOrNull { it.id == selected_session_id }
    val snackbar = remember { SnackbarHostState() }
    val launch_message = launch_message_res_id?.let { stringResource(it) }
    var is_settings_visible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(launch_message) {
        if (launch_message != null) {
            snackbar.showSnackbar(launch_message)
            view_model.clear_launch_message()
        }
    }
    if (is_add_visible) add_session_dialog(installed_games, view_model::dismiss_add_session, view_model::add_session)
    if (is_onboarding_visible) onboarding_dialog(view_model::complete_onboarding)
    if (is_settings_visible) settings_dialog(remaining_hours, profile_status, view_model::open_android_settings) { is_settings_visible = false }
    if (is_delete_visible && selected_session != null) {
        delete_session_dialog(selected_session.name, view_model::delete_selected_session, view_model::dismiss_delete_confirmation)
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        game_library(
            sessions = sessions,
            remaining_quota_hours = remaining_hours,
            content_padding = padding,
            on_launch = view_model::launch_session,
            on_add = { view_model.show_add_session() },
            on_delete = view_model::request_delete_session,
            on_settings = { is_settings_visible = true },
        )
    }
}
