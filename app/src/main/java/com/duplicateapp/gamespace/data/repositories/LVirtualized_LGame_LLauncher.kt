package com.duplicateapp.gamespace.features.workspace.data

import com.duplicateapp.gamespace.features.virtualization.domain.virtual_game_runtime
import com.duplicateapp.gamespace.features.virtualization.domain.virtual_runtime_result
import com.duplicateapp.gamespace.features.workspace.domain.game_launch_result
import com.duplicateapp.gamespace.features.workspace.domain.game_launcher
import com.duplicateapp.gamespace.features.workspace.domain.game_session
import com.duplicateapp.gamespace.features.workspace.domain.launch_unavailable_reason
import com.duplicateapp.gamespace.features.workspace.domain.profile_target

class virtualized_game_launcher(
    private val runtime: virtual_game_runtime,
) : game_launcher {
    override fun launch(session: game_session): game_launch_result {
        val virtual_user_id: Int = when (session.profile_target) {
            profile_target.personal -> origin_virtual_user_id
            profile_target.managed -> first_copy_virtual_user_id
        }
        if (!runtime.is_installed(session.package_name, virtual_user_id)) {
            when (runtime.install_from_device(session.package_name, virtual_user_id)) {
                is virtual_runtime_result.failure -> return game_launch_result.unavailable(launch_unavailable_reason.permission_denied)
                virtual_runtime_result.success -> Unit
            }
        }
        return when (runtime.launch(session.package_name, virtual_user_id)) {
            is virtual_runtime_result.failure -> game_launch_result.unavailable(launch_unavailable_reason.permission_denied)
            virtual_runtime_result.success -> game_launch_result.opened(session.profile_target)
        }
    }

    private companion object {
        const val origin_virtual_user_id = 0
        const val first_copy_virtual_user_id = 1
    }
}
