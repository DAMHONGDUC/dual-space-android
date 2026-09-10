package com.duplicateapp.gamespace.features.workspace.domain

import kotlinx.coroutines.flow.StateFlow

interface workspace_repository {
    val sessions: StateFlow<List<game_session>>
    val selected_session_id: StateFlow<String?>

    suspend fun select_session(session_id: String)
    suspend fun update_session_state(session_id: String, state: session_state)
    suspend fun add_session(name: String, game_name: String, package_name: String, profile_target: profile_target)
    suspend fun delete_session(session_id: String)
}
