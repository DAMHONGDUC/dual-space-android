package com.duplicateapp.gamespace.features.workspace.data

import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import com.duplicateapp.gamespace.core.logging.AppLogger
import com.duplicateapp.gamespace.features.workspace.domain.GameLaunchResult
import com.duplicateapp.gamespace.features.workspace.domain.GameLauncher
import com.duplicateapp.gamespace.features.workspace.domain.GameSession
import com.duplicateapp.gamespace.features.workspace.domain.LaunchUnavailableReason
import com.duplicateapp.gamespace.features.workspace.domain.ProfileTarget

class AndroidProfileGameLauncher(context: Context) : GameLauncher {
    private val launcherApps: LauncherApps = context.getSystemService(LauncherApps::class.java)
    private val userManager: UserManager = context.getSystemService(UserManager::class.java)

    override fun launch(session: GameSession): GameLaunchResult {
        AppLogger.action("launch_profile_game", mapOf("sessionId" to session.id, "profile" to session.profileTarget.name))
        val profile: UserHandle = findProfile(session.profileTarget)
            ?: return unavailable(session, LaunchUnavailableReason.missingManagedProfile)
        val activity: LauncherActivityInfo = launcherApps.getActivityList(session.packageName, profile).firstOrNull()
            ?: return unavailable(session, LaunchUnavailableReason.gameNotInstalled)

        return try {
            launcherApps.startMainActivity(activity.componentName, profile, null, null)
            AppLogger.success("launch_profile_game", mapOf("sessionId" to session.id, "profile" to session.profileTarget.name))
            GameLaunchResult.Opened(session.profileTarget)
        } catch (error: Exception) {
            AppLogger.error("launch_profile_game", error, mapOf("sessionId" to session.id))
            GameLaunchResult.Unavailable(LaunchUnavailableReason.permissionDenied)
        }
    }

    private fun findProfile(target: ProfileTarget): UserHandle? {
        val personalUser: UserHandle = Process.myUserHandle()
        return when (target) {
            ProfileTarget.personal -> personalUser
            ProfileTarget.managed -> userManager.userProfiles.firstOrNull { profile -> profile != personalUser }
        }
    }

    private fun unavailable(session: GameSession, reason: LaunchUnavailableReason): GameLaunchResult.Unavailable {
        AppLogger.error("launch_profile_game_unavailable", IllegalStateException(reason.name), mapOf("sessionId" to session.id))
        return GameLaunchResult.Unavailable(reason)
    }
}
