package com.dd.dual.space.features.workspace.data

import com.dd.dual.space.features.virtualization.domain.VirtualGameRuntime
import com.dd.dual.space.features.virtualization.domain.VirtualRuntimeResult
import com.dd.dual.space.features.workspace.domain.GameLaunchReadiness
import com.dd.dual.space.features.workspace.domain.GameLaunchResult
import com.dd.dual.space.features.workspace.domain.GameLauncher
import com.dd.dual.space.features.workspace.domain.GameCopyRemover
import com.dd.dual.space.features.workspace.domain.GameSession
import com.dd.dual.space.features.workspace.domain.LaunchUnavailableReason

class VirtualizedGameLauncher(
    private val runtime: VirtualGameRuntime,
) : GameLauncher, GameCopyRemover {
    override fun readiness(session: GameSession): GameLaunchReadiness = GameLaunchReadiness.Ready

    override fun isRunning(session: GameSession): Boolean =
        runtime.isRunning(session.packageName, session.virtualUserId)

    override fun launch(session: GameSession): GameLaunchResult {
        if (!runtime.isInstalled(session.packageName, session.virtualUserId)) {
            when (runtime.installFromDevice(session.packageName, session.virtualUserId)) {
                is VirtualRuntimeResult.Failure -> return GameLaunchResult.Unavailable(LaunchUnavailableReason.gameNotInstalled)
                VirtualRuntimeResult.Success -> Unit
            }
        }
        return when (runtime.launch(session.packageName, session.virtualUserId)) {
            is VirtualRuntimeResult.Failure -> GameLaunchResult.Unavailable(LaunchUnavailableReason.permissionDenied)
            VirtualRuntimeResult.Success -> GameLaunchResult.Opened(session.profileTarget)
        }
    }

    override fun remove(session: GameSession): Boolean =
        when (runtime.uninstall(session.packageName, session.virtualUserId)) {
            is VirtualRuntimeResult.Failure -> false
            VirtualRuntimeResult.Success -> true
        }
}
