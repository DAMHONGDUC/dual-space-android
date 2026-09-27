package com.dd.dual.space.features.workspace.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.os.Process
import com.dd.dual.space.core.logging.AppLogger
import com.dd.dual.space.features.workspace.domain.GameCatalog
import com.dd.dual.space.features.workspace.domain.InstalledGame
import com.dd.dual.space.features.workspace.domain.ProfileTarget

class AndroidGameCatalog(
    context: Context,
) : GameCatalog {
    private val launcherApps: LauncherApps = context.getSystemService(LauncherApps::class.java)
    private val hostPackageName: String = context.packageName

    override fun listInstalledGames(profileTarget: ProfileTarget): List<InstalledGame> {
        return try {
            launcherApps.getActivityList(null, Process.myUserHandle())
                .distinctBy { activity -> activity.applicationInfo.packageName }
                .filterNot { activity -> activity.applicationInfo.packageName == hostPackageName }
                .sortedWith(
                    compareBy<LauncherActivityInfo>(
                        { activity -> activity.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0 },
                        { activity -> activity.label.toString().lowercase() },
                    ),
                )
                .map { activity ->
                    val packageName: String = activity.applicationInfo.packageName
                    InstalledGame(
                        label = activity.label.toString(),
                        packageName = packageName,
                        profileTarget = ProfileTarget.managed,
                        isCopyAvailable = true,
                    )
                }
                .also { games -> AppLogger.success("list_installed_games", mapOf("profile" to profileTarget.name, "count" to games.size)) }
        } catch (error: Exception) {
            AppLogger.error("list_installed_games", error, mapOf("profile" to profileTarget.name))
            emptyList()
        }
    }

}
