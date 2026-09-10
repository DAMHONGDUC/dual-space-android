package com.duplicateapp.gamespace.features.workspace.domain

data class installed_game(
    val label: String,
    val package_name: String,
    val profile_target: profile_target,
)

interface game_catalog {
    fun list_installed_games(profile_target: profile_target): List<installed_game>
}
