package com.dd.dual.space.features.workspace.data

import com.dd.dual.space.features.virtualization.domain.FailureKind
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
    // A copy that is not prepared yet can only be created while the original game is on the device.
    override fun readiness(session: GameSession): GameLaunchReadiness =
        if (!runtime.isInstalled(session.packageName, session.virtualUserId) && !runtime.isSourceInstalled(session.packageName)) {
            GameLaunchReadiness.Unavailable(LaunchUnavailableReason.gameNotInstalled)
        } else {
            GameLaunchReadiness.Ready
        }

    override fun isRunning(session: GameSession): Boolean =
        runtime.isRunning(session.packageName, session.virtualUserId)

    override fun launch(session: GameSession): GameLaunchResult {
        if (!runtime.isInstalled(session.packageName, session.virtualUserId)) {
            when (val installed = runtime.installFromDevice(session.packageName, session.virtualUserId)) {
                is VirtualRuntimeResult.Failure -> return GameLaunchResult.Unavailable(reasonFor(installed.kind))
                VirtualRuntimeResult.Success -> Unit
            }
        }
        return when (val launched = runtime.launch(session.packageName, session.virtualUserId)) {
            is VirtualRuntimeResult.Failure -> GameLaunchResult.Unavailable(reasonFor(launched.kind))
            VirtualRuntimeResult.Success -> GameLaunchResult.Opened(session.profileTarget)
        }
    }

    private fun reasonFor(kind: FailureKind): LaunchUnavailableReason = when (kind) {
        FailureKind.sourceMissing -> LaunchUnavailableReason.gameNotInstalled
        FailureKind.installFailed -> LaunchUnavailableReason.installFailed
        FailureKind.launchTimedOut -> LaunchUnavailableReason.launchTimedOut
        FailureKind.engineUnavailable, FailureKind.unknown -> LaunchUnavailableReason.engineUnavailable
    }

    override fun remove(session: GameSession): Boolean =
        when (runtime.uninstall(session.packageName, session.virtualUserId)) {
            is VirtualRuntimeResult.Failure -> false
            VirtualRuntimeResult.Success -> true
        }
}
