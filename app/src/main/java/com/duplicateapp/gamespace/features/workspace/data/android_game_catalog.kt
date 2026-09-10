package com.duplicateapp.gamespace.features.workspace.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import com.duplicateapp.gamespace.core.logging.AppLogger
import com.duplicateapp.gamespace.features.workspace.domain.GameCatalog
import com.duplicateapp.gamespace.features.workspace.domain.InstalledGame
import com.duplicateapp.gamespace.features.workspace.domain.ProfileTarget

class AndroidGameCatalog(context: Context) : GameCatalog {
    private val launcherApps: LauncherApps = context.getSystemService(LauncherApps::class.java)
    private val userManager: UserManager = context.getSystemService(UserManager::class.java)
    private val hostPackageName: String = context.packageName

    override fun listInstalledGames(profileTarget: ProfileTarget): List<InstalledGame> {
        val profile: UserHandle = resolveProfile(profileTarget) ?: return emptyList()
        return try {
            launcherApps.getActivityList(null, profile)
                .distinctBy { activity -> activity.applicationInfo.packageName }
                .filterNot { activity -> activity.applicationInfo.packageName == hostPackageName }
                .filter { activity -> activity.applicationInfo.category == ApplicationInfo.CATEGORY_GAME }
                .sortedWith(
                    compareBy<LauncherActivityInfo>(
                        { activity -> activity.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0 },
                        { activity -> activity.label.toString().lowercase() },
                    ),
                )
                .map { activity ->
                    InstalledGame(activity.label.toString(), activity.applicationInfo.packageName, profileTarget)
                }
                .also { games -> AppLogger.success("list_installed_games", mapOf("profile" to profileTarget.name, "count" to games.size)) }
        } catch (error: Exception) {
            AppLogger.error("list_installed_games", error, mapOf("profile" to profileTarget.name))
            emptyList()
        }
    }

    private fun resolveProfile(target: ProfileTarget): UserHandle? = when (target) {
        ProfileTarget.personal -> Process.myUserHandle()
        ProfileTarget.managed -> userManager.userProfiles.firstOrNull { profile -> profile != Process.myUserHandle() }
    }
}
