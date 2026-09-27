package com.dd.dual.space.features.workspace.domain

interface GameLauncher {
    fun readiness(session: GameSession): GameLaunchReadiness
    fun launch(session: GameSession): GameLaunchResult
    fun isRunning(session: GameSession): Boolean = false
}

sealed interface GameLaunchReadiness {
    data object Ready : GameLaunchReadiness
    data class Unavailable(val reason: LaunchUnavailableReason) : GameLaunchReadiness
}

sealed interface GameLaunchResult {
    data class Opened(val profile: ProfileTarget) : GameLaunchResult
    data class Unavailable(val reason: LaunchUnavailableReason) : GameLaunchResult
}

enum class LaunchUnavailableReason {
    missingManagedProfile,
    gameNotInstalled,
    permissionDenied,
    engineUnavailable,
    installFailed,
    launchTimedOut,
}
