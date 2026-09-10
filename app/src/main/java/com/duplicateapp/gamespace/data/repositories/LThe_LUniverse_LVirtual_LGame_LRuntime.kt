package com.duplicateapp.gamespace.features.virtualization.data

import com.duplicateapp.gamespace.core.logging.app_logger
import com.duplicateapp.gamespace.features.virtualization.domain.virtual_game_runtime
import com.duplicateapp.gamespace.features.virtualization.domain.virtual_runtime_result
import com.duplicateapp.theuniverse.TheUniverseCore
import com.duplicateapp.theuniverse.entity.pm.InstallResult

class the_universe_virtual_game_runtime : virtual_game_runtime {
    override fun install_from_device(package_name: String, virtual_user_id: Int): virtual_runtime_result {
        app_logger.action("virtual_game_install", metadata(package_name, virtual_user_id))
        return try {
            val result: InstallResult = TheUniverseCore.get().installPackageAsUser(package_name, virtual_user_id)
            if (result.success) {
                app_logger.success("virtual_game_install", metadata(package_name, virtual_user_id))
                virtual_runtime_result.success
            } else {
                val error = IllegalStateException(result.msg ?: "Virtual install failed")
                app_logger.error("virtual_game_install", error, metadata(package_name, virtual_user_id))
                virtual_runtime_result.failure(result.msg)
            }
        } catch (error: Exception) {
            app_logger.error("virtual_game_install", error, metadata(package_name, virtual_user_id))
            virtual_runtime_result.failure(error.message)
        }
    }

    override fun is_installed(package_name: String, virtual_user_id: Int): Boolean {
        return try {
            TheUniverseCore.get().isInstalled(package_name, virtual_user_id)
        } catch (error: Exception) {
            app_logger.error("virtual_game_is_installed", error, metadata(package_name, virtual_user_id))
            false
        }
    }

    override fun launch(package_name: String, virtual_user_id: Int): virtual_runtime_result {
        app_logger.action("virtual_game_launch", metadata(package_name, virtual_user_id))
        return try {
            if (TheUniverseCore.get().launchApk(package_name, virtual_user_id)) {
                app_logger.success("virtual_game_launch", metadata(package_name, virtual_user_id))
                virtual_runtime_result.success
            } else {
                val error = IllegalStateException("Virtual launch failed")
                app_logger.error("virtual_game_launch", error, metadata(package_name, virtual_user_id))
                virtual_runtime_result.failure(error.message)
            }
        } catch (error: Exception) {
            app_logger.error("virtual_game_launch", error, metadata(package_name, virtual_user_id))
            virtual_runtime_result.failure(error.message)
        }
    }

    private fun metadata(package_name: String, virtual_user_id: Int): Map<String, Any> = mapOf(
        "packageName" to package_name,
        "virtualUserId" to virtual_user_id,
    )
}
