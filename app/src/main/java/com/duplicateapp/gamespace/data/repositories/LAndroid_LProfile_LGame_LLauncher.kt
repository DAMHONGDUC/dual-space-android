package com.duplicateapp.gamespace.features.workspace.data

import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import com.duplicateapp.gamespace.core.logging.app_logger
import com.duplicateapp.gamespace.features.workspace.domain.game_launch_result
import com.duplicateapp.gamespace.features.workspace.domain.game_launcher
import com.duplicateapp.gamespace.features.workspace.domain.game_session
import com.duplicateapp.gamespace.features.workspace.domain.launch_unavailable_reason
import com.duplicateapp.gamespace.features.workspace.domain.profile_target

class android_profile_game_launcher(context: Context) : game_launcher {
    private val launcher_apps: LauncherApps = context.getSystemService(LauncherApps::class.java)
    private val user_manager: UserManager = context.getSystemService(UserManager::class.java)

    override fun launch(session: game_session): game_launch_result {
        app_logger.action("launch_profile_game", mapOf("sessionId" to session.id, "profile" to session.profile_target.name))
        val profile: UserHandle = find_profile(session.profile_target)
            ?: return unavailable(session, launch_unavailable_reason.missing_managed_profile)
        val activity: LauncherActivityInfo = launcher_apps.getActivityList(session.package_name, profile).firstOrNull()
            ?: return unavailable(session, launch_unavailable_reason.game_not_installed)

        return try {
            launcher_apps.startMainActivity(activity.componentName, profile, null, null)
            app_logger.success("launch_profile_game", mapOf("sessionId" to session.id, "profile" to session.profile_target.name))
            game_launch_result.opened(session.profile_target)
        } catch (error: Exception) {
            app_logger.error("launch_profile_game", error, mapOf("sessionId" to session.id))
            game_launch_result.unavailable(launch_unavailable_reason.permission_denied)
        }
    }

    private fun find_profile(target: profile_target): UserHandle? {
        val personal_user: UserHandle = Process.myUserHandle()
        return when (target) {
            profile_target.personal -> personal_user
            profile_target.managed -> user_manager.userProfiles.firstOrNull { profile -> profile != personal_user }
        }
    }

    private fun unavailable(session: game_session, reason: launch_unavailable_reason): game_launch_result.unavailable {
        app_logger.error("launch_profile_game_unavailable", IllegalStateException(reason.name), mapOf("sessionId" to session.id))
        return game_launch_result.unavailable(reason)
    }
}
