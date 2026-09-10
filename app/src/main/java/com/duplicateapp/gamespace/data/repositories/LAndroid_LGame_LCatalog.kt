package com.duplicateapp.gamespace.features.workspace.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import com.duplicateapp.gamespace.core.logging.app_logger
import com.duplicateapp.gamespace.features.workspace.domain.game_catalog
import com.duplicateapp.gamespace.features.workspace.domain.installed_game
import com.duplicateapp.gamespace.features.workspace.domain.profile_target

class android_game_catalog(context: Context) : game_catalog {
    private val launcher_apps: LauncherApps = context.getSystemService(LauncherApps::class.java)
    private val user_manager: UserManager = context.getSystemService(UserManager::class.java)
    private val host_package_name: String = context.packageName

    override fun list_installed_games(profile_target: profile_target): List<installed_game> {
        val profile: UserHandle = resolve_profile(profile_target) ?: return emptyList()
        return try {
            launcher_apps.getActivityList(null, profile)
                .distinctBy { activity -> activity.applicationInfo.packageName }
                .filterNot { activity -> activity.applicationInfo.packageName == host_package_name }
                .filter { activity -> activity.applicationInfo.category == ApplicationInfo.CATEGORY_GAME }
                .sortedWith(
                    compareBy<LauncherActivityInfo>(
                        { activity -> activity.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0 },
                        { activity -> activity.label.toString().lowercase() },
                    ),
                )
                .map { activity ->
                    installed_game(activity.label.toString(), activity.applicationInfo.packageName, profile_target)
                }
                .also { games -> app_logger.success("list_installed_games", mapOf("profile" to profile_target.name, "count" to games.size)) }
        } catch (error: Exception) {
            app_logger.error("list_installed_games", error, mapOf("profile" to profile_target.name))
            emptyList()
        }
    }

    private fun resolve_profile(target: profile_target): UserHandle? = when (target) {
        profile_target.personal -> Process.myUserHandle()
        profile_target.managed -> user_manager.userProfiles.firstOrNull { profile -> profile != Process.myUserHandle() }
    }
}
