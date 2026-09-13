package com.duplicateapp.gamespace.features.virtualization.data

import com.duplicateapp.gamespace.core.logging.AppLogger
import com.duplicateapp.gamespace.features.virtualization.domain.VirtualGameRuntime
import com.duplicateapp.gamespace.features.virtualization.domain.VirtualRuntimeResult
import com.duplicateapp.theuniverse.TheUniverseCore
import com.duplicateapp.theuniverse.entity.pm.InstallResult

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
            if (TheUniverseCore.get().launchApk(packageName, virtualUserId)) {
                AppLogger.success("virtual_game_launch", metadata)
                VirtualRuntimeResult.Success
            } else {
                val error = IllegalStateException("Virtual launch failed")
                AppLogger.error("virtual_game_launch", error, metadata)
                VirtualRuntimeResult.Failure(error.message)
            }
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
}
