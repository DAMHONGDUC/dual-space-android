package com.duplicateapp.gamespace.features.virtualization.data

import android.os.SystemClock
import com.duplicateapp.gamespace.core.logging.AppLogger
import com.duplicateapp.gamespace.features.virtualization.domain.VirtualGameRuntime
import com.duplicateapp.gamespace.features.virtualization.domain.VirtualRuntimeResult
import com.dd.the.universe.TheUniverseCore
import com.dd.the.universe.entity.pm.InstallResult

class TheUniverseVirtualGameRuntime : VirtualGameRuntime {
    override fun installFromDevice(packageName: String, virtualUserId: Int): VirtualRuntimeResult {
        val metadata: Map<String, Any> = metadata(packageName, virtualUserId)

        AppLogger.action("virtual_game_install", metadata)
        return try {
            val result: InstallResult = TheUniverseCore.get().installPackageAsUser(packageName, virtualUserId)
            if (result.success) {
                AppLogger.success("virtual_game_install", metadata)
                VirtualRuntimeResult.Success
            } else {
                val error = IllegalStateException(result.msg ?: "Virtual install failed")
                AppLogger.error("virtual_game_install", error, metadata)
                VirtualRuntimeResult.Failure(result.msg)
            }
        } catch (error: Exception) {
            AppLogger.error("virtual_game_install", error, metadata)
            VirtualRuntimeResult.Failure(error.message)
        }
    }

    override fun isInstalled(packageName: String, virtualUserId: Int): Boolean = try {
        TheUniverseCore.get().isInstalled(packageName, virtualUserId)
    } catch (error: Exception) {
        AppLogger.error("virtual_game_is_installed", error, metadata(packageName, virtualUserId))
        false
    }

    override fun launch(packageName: String, virtualUserId: Int): VirtualRuntimeResult {
        val metadata: Map<String, Any> = metadata(packageName, virtualUserId)

        AppLogger.action("virtual_game_launch", metadata)
        return try {
            val service = TheUniverseCore.getBActivityManager().service
                ?: return VirtualRuntimeResult.Failure("Virtual activity service unavailable")
            val previousSequence: Long = service.getActivityResumeSequence(packageName, virtualUserId)
            val dispatched: Boolean = TheUniverseCore.get().launchApk(packageName, virtualUserId)
            val confirmed: Boolean = dispatched && LaunchConfirmation(
                nowMillis = SystemClock::elapsedRealtime,
                pause = Thread::sleep,
            ).await(launchTimeoutMillis) {
                service.getActivityResumeSequence(packageName, virtualUserId) > previousSequence
            }
            if (confirmed) {
                AppLogger.success("virtual_game_launch", metadata)
                VirtualRuntimeResult.Success
            } else {
                val error = IllegalStateException("Virtual launch failed")
                AppLogger.error("virtual_game_launch", error, metadata)
                VirtualRuntimeResult.Failure(error.message)
            }
        } catch (error: InterruptedException) {
            Thread.currentThread().interrupt()
            AppLogger.error("virtual_game_launch_interrupted", error, metadata)
            VirtualRuntimeResult.Failure("Launch interrupted")
        } catch (error: Exception) {
            AppLogger.error("virtual_game_launch", error, metadata)
            VirtualRuntimeResult.Failure(error.message)
        }
    }

    override fun isRunning(packageName: String, virtualUserId: Int): Boolean = try {
        TheUniverseCore.isRunningApplication(packageName, virtualUserId)
    } catch (error: Exception) {
        AppLogger.error("virtual_game_is_running", error, metadata(packageName, virtualUserId))
        false
    }

    override fun uninstall(packageName: String, virtualUserId: Int): VirtualRuntimeResult {
        val metadata: Map<String, Any> = metadata(packageName, virtualUserId)

        AppLogger.action("virtual_game_uninstall", metadata)
        return try {
            val core: TheUniverseCore = TheUniverseCore.get()
            core.uninstallPackageAsUser(packageName, virtualUserId)
            if (!core.isInstalled(packageName, virtualUserId)) {
                AppLogger.success("virtual_game_uninstall", metadata)
                VirtualRuntimeResult.Success
            } else {
                val error = IllegalStateException("Virtual uninstall failed")
                AppLogger.error("virtual_game_uninstall", error, metadata)
                VirtualRuntimeResult.Failure(error.message)
            }
        } catch (error: Exception) {
            AppLogger.error("virtual_game_uninstall", error, metadata)
            VirtualRuntimeResult.Failure(error.message)
        }
    }

    private fun metadata(packageName: String, virtualUserId: Int): Map<String, Any> = mapOf(
        "packageName" to packageName,
        "virtualUserId" to virtualUserId,
    )

    private companion object {
        const val launchTimeoutMillis: Long = 10_000L
    }
}
