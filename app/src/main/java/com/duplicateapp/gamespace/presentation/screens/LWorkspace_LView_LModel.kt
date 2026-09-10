package com.duplicateapp.gamespace.features.workspace.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.content.Context
import com.duplicateapp.gamespace.features.workspace.data.virtualized_game_launcher
import com.duplicateapp.gamespace.features.workspace.data.android_profile_provisioner
import com.duplicateapp.gamespace.features.workspace.data.android_game_catalog
import com.duplicateapp.gamespace.features.workspace.domain.game_launch_result
import com.duplicateapp.gamespace.features.workspace.domain.game_launcher
import com.duplicateapp.gamespace.features.workspace.domain.launch_unavailable_reason
import com.duplicateapp.gamespace.features.workspace.domain.profile_provisioner
import com.duplicateapp.gamespace.features.workspace.domain.profile_provisioning_status
import com.duplicateapp.gamespace.features.workspace.domain.game_catalog
import com.duplicateapp.gamespace.features.workspace.domain.installed_game
import com.duplicateapp.gamespace.features.workspace.domain.profile_target
import androidx.lifecycle.viewModelScope
import com.duplicateapp.gamespace.features.quota.data.local_quota_tracker
import com.duplicateapp.gamespace.features.onboarding.data.onboarding_store
import com.duplicateapp.gamespace.features.workspace.data.persistent_workspace_repository
import com.duplicateapp.gamespace.features.workspace.domain.game_session
import com.duplicateapp.gamespace.features.workspace.domain.workspace_repository
import com.duplicateapp.gamespace.features.workspace.domain.settings_navigator
import com.duplicateapp.gamespace.features.workspace.domain.session_state
import com.duplicateapp.gamespace.features.workspace.data.android_settings_navigator
import com.duplicateapp.gamespace.features.virtualization.data.the_universe_virtual_game_runtime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class workspace_view_model(
    private val repository: workspace_repository,
    private val game_launcher: game_launcher,
    private val profile_provisioner: profile_provisioner,
    private val game_catalog: game_catalog,
    private val quota_tracker: local_quota_tracker,
    private val onboarding_store: onboarding_store,
    private val settings_navigator: settings_navigator,
) : ViewModel() {
    private val mutable_selected_session_id = MutableStateFlow(repository.selected_session_id.value)
    private val mutable_launch_message = MutableStateFlow<Int?>(null)
    private val mutable_profile_provisioning_status = MutableStateFlow(profile_provisioner.status())
    private val mutable_installed_games = MutableStateFlow<List<installed_game>>(emptyList())
    private val mutable_is_add_session_visible = MutableStateFlow(false)
    private val mutable_remaining_quota_hours = MutableStateFlow(quota_tracker.remaining_hours())
    private val mutable_is_onboarding_visible = MutableStateFlow(!onboarding_store.is_complete())
    private val mutable_is_delete_confirmation_visible = MutableStateFlow(false)

    val sessions: StateFlow<List<game_session>> = repository.sessions
    val selected_session_id: StateFlow<String?> = mutable_selected_session_id.asStateFlow()
    val launch_message: StateFlow<Int?> = mutable_launch_message.asStateFlow()
    val remaining_quota_hours: StateFlow<Int> = mutable_remaining_quota_hours.asStateFlow()
    val profile_provisioning_status: StateFlow<profile_provisioning_status> = mutable_profile_provisioning_status.asStateFlow()
    val installed_games: StateFlow<List<installed_game>> = mutable_installed_games.asStateFlow()
    val is_add_session_visible: StateFlow<Boolean> = mutable_is_add_session_visible.asStateFlow()
    val is_onboarding_visible: StateFlow<Boolean> = mutable_is_onboarding_visible.asStateFlow()
    val is_delete_confirmation_visible: StateFlow<Boolean> = mutable_is_delete_confirmation_visible.asStateFlow()

    fun select_session(session_id: String) {
        mutable_selected_session_id.value = session_id
        viewModelScope.launch {
            repository.select_session(session_id)
        }
    }

    fun toggle_selected_session() {
        val session: game_session = sessions.value.firstOrNull { item -> item.id == mutable_selected_session_id.value } ?: return
        launch(session)
    }

    fun launch_session(session_id: String) {
        val session: game_session = sessions.value.firstOrNull { item -> item.id == session_id } ?: return
        mutable_selected_session_id.value = session_id
        viewModelScope.launch { repository.select_session(session_id) }
        launch(session)
    }

    private fun launch(session: game_session) {
        if (!quota_tracker.can_launch()) {
            mutable_launch_message.value = com.duplicateapp.gamespace.R.string.quota_exhausted
            return
        }
        viewModelScope.launch {
            repository.update_session_state(session.id, session_state.starting)
            mutable_launch_message.value = when (val result: game_launch_result = game_launcher.launch(session)) {
                is game_launch_result.opened -> {
                    repository.update_session_state(session.id, session_state.running)
                    quota_tracker.start_session()
                    com.duplicateapp.gamespace.R.string.game_opened
                }
                is game_launch_result.unavailable -> {
                    repository.update_session_state(session.id, session_state.failed)
                    when (result.reason) {
                        launch_unavailable_reason.missing_managed_profile -> com.duplicateapp.gamespace.R.string.missing_managed_profile
                        launch_unavailable_reason.game_not_installed -> com.duplicateapp.gamespace.R.string.game_not_installed
                        launch_unavailable_reason.permission_denied -> com.duplicateapp.gamespace.R.string.profile_permission_denied
                    }
                }
            }
        }
    }

    fun clear_launch_message() {
        mutable_launch_message.value = null
    }

    fun create_profile() {
        if (!profile_provisioner.request_creation()) {
            mutable_launch_message.value = com.duplicateapp.gamespace.R.string.profile_creation_failed
        }
    }

    fun refresh_profile_status() {
        quota_tracker.settle_session()
        mutable_remaining_quota_hours.value = quota_tracker.remaining_hours()
        mutable_profile_provisioning_status.value = profile_provisioner.status()
        viewModelScope.launch {
            sessions.value.filter { session -> session.state == session_state.running }
                .forEach { session -> repository.update_session_state(session.id, session_state.paused) }
        }
    }

    fun complete_onboarding() {
        onboarding_store.complete()
        mutable_is_onboarding_visible.value = false
    }

    fun show_add_session(profile_target: profile_target = profile_target.personal) {
        mutable_installed_games.value = game_catalog.list_installed_games(profile_target)
        mutable_is_add_session_visible.value = true
    }

    fun change_catalog_profile(profile_target: profile_target) {
        mutable_installed_games.value = game_catalog.list_installed_games(profile_target)
    }

    fun dismiss_add_session() {
        mutable_is_add_session_visible.value = false
    }

    fun add_session(name: String, game: installed_game) {
        val existing_copies: List<game_session> = sessions.value.filter { session -> session.package_name == game.package_name }
        if (existing_copies.size >= maximum_copies_per_game) {
            mutable_launch_message.value = com.duplicateapp.gamespace.R.string.game_already_added
            return
        }
        val target: profile_target = if (existing_copies.isEmpty()) profile_target.personal else profile_target.managed
        val copy_name: String = if (target == profile_target.personal) name else "$name · Copy 1"
        viewModelScope.launch {
            repository.add_session(copy_name, game.label, game.package_name, target)
            val added_session_id: String = repository.sessions.value.last().id
            mutable_selected_session_id.value = added_session_id
            repository.select_session(added_session_id)
            mutable_is_add_session_visible.value = false
        }
    }

    fun delete_selected_session() {
        val session_id: String = mutable_selected_session_id.value ?: return
        viewModelScope.launch {
            repository.delete_session(session_id)
            mutable_selected_session_id.value = repository.sessions.value.firstOrNull()?.id
            mutable_is_delete_confirmation_visible.value = false
        }
    }

    fun request_delete_selected_session() {
        if (sessions.value.isEmpty()) return
        mutable_is_delete_confirmation_visible.value = true
    }

    fun request_delete_session(session_id: String) {
        mutable_selected_session_id.value = session_id
        viewModelScope.launch { repository.select_session(session_id) }
        mutable_is_delete_confirmation_visible.value = true
    }

    fun dismiss_delete_confirmation() {
        mutable_is_delete_confirmation_visible.value = false
    }

    fun open_android_settings() {
        if (!settings_navigator.open_android_settings()) {
            mutable_launch_message.value = com.duplicateapp.gamespace.R.string.settings_open_failed
        }
    }

    class factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return workspace_view_model(
                persistent_workspace_repository(context),
                virtualized_game_launcher(the_universe_virtual_game_runtime()),
                android_profile_provisioner(context),
                android_game_catalog(context),
                local_quota_tracker(context),
                onboarding_store(context),
                android_settings_navigator(context),
            ) as T
        }
    }

    private companion object {
        const val maximum_copies_per_game = 2
    }
}
