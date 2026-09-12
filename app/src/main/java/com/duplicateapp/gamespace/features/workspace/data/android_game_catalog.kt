package com.duplicateapp.gamespace.features.workspace.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.os.UserHandle
import com.duplicateapp.gamespace.core.logging.AppLogger
import com.duplicateapp.gamespace.features.workspace.domain.GameCatalog
import com.duplicateapp.gamespace.features.workspace.domain.InstalledGame
import com.duplicateapp.gamespace.features.workspace.domain.ProfileTarget

class AndroidGameCatalog(
    context: Context,
    private val profileResolver: AndroidManagedProfileResolver = AndroidManagedProfileResolver(context),
) : GameCatalog {
    private val launcherApps: LauncherApps = context.getSystemService(LauncherApps::class.java)
    private val hostPackageName: String = context.packageName

    override fun listInstalledGames(profileTarget: ProfileTarget): List<InstalledGame> {
        val profile: UserHandle = profileResolver.resolve(profileTarget) ?: return emptyList()
        val managedProfile: UserHandle? = profileResolver.resolve(ProfileTarget.managed)
        return try {
            val managedPackages: Set<String> = managedProfile?.let { target ->
                launcherApps.getActivityList(null, target).map { activity -> activity.applicationInfo.packageName }.toSet()
            }.orEmpty()
            launcherApps.getActivityList(null, profile)
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
                        profileTarget = profileTarget,
                        isCopyAvailable = packageName in managedPackages,
                    )
                }
                .also { games -> AppLogger.success("list_installed_games", mapOf("profile" to profileTarget.name, "count" to games.size)) }
        } catch (error: Exception) {
            AppLogger.error("list_installed_games", error, mapOf("profile" to profileTarget.name))
            emptyList()
        }
    }

}
