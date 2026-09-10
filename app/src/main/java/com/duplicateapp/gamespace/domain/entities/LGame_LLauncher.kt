package com.duplicateapp.gamespace.features.workspace.domain

interface game_launcher {
    fun launch(session: game_session): game_launch_result
}

sealed interface game_launch_result {
    data class opened(val profile: profile_target) : game_launch_result
    data class unavailable(val reason: launch_unavailable_reason) : game_launch_result
}

enum class launch_unavailable_reason {
    missing_managed_profile,
    game_not_installed,
    permission_denied,
}
