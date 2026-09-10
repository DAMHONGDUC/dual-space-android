package com.duplicateapp.gamespace.features.workspace.domain

interface GameLauncher {
    fun launch(session: GameSession): GameLaunchResult
}

sealed interface GameLaunchResult {
    data class Opened(val profile: ProfileTarget) : GameLaunchResult
    data class Unavailable(val reason: LaunchUnavailableReason) : GameLaunchResult
}

enum class LaunchUnavailableReason {
    missingManagedProfile,
    gameNotInstalled,
    permissionDenied,
}
