package com.duplicateapp.gamespace.features.workspace.data

import com.duplicateapp.gamespace.features.virtualization.domain.VirtualGameRuntime
import com.duplicateapp.gamespace.features.virtualization.domain.VirtualRuntimeResult
import com.duplicateapp.gamespace.features.workspace.domain.GameLaunchResult
import com.duplicateapp.gamespace.features.workspace.domain.GameLauncher
import com.duplicateapp.gamespace.features.workspace.domain.GameSession
import com.duplicateapp.gamespace.features.workspace.domain.LaunchUnavailableReason
import com.duplicateapp.gamespace.features.workspace.domain.ProfileTarget

class VirtualizedGameLauncher(
    private val runtime: VirtualGameRuntime,
) : GameLauncher {
    override fun launch(session: GameSession): GameLaunchResult {
        val virtualUserId: Int = when (session.profileTarget) {
            ProfileTarget.personal -> originVirtualUserId
            ProfileTarget.managed -> firstCopyVirtualUserId
        }
        if (!runtime.isInstalled(session.packageName, virtualUserId)) {
            when (runtime.installFromDevice(session.packageName, virtualUserId)) {
                is VirtualRuntimeResult.Failure -> return GameLaunchResult.Unavailable(LaunchUnavailableReason.permissionDenied)
                VirtualRuntimeResult.Success -> Unit
            }
        }
        return when (runtime.launch(session.packageName, virtualUserId)) {
            is VirtualRuntimeResult.Failure -> GameLaunchResult.Unavailable(LaunchUnavailableReason.permissionDenied)
            VirtualRuntimeResult.Success -> GameLaunchResult.Opened(session.profileTarget)
        }
    }

    private companion object {
        const val originVirtualUserId = 0
        const val firstCopyVirtualUserId = 1
    }
}
