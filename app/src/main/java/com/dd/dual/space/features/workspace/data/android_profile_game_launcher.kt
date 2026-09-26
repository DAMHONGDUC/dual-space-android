package com.dd.dual.space.features.workspace.data

import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.os.UserHandle
import com.dd.dual.space.core.logging.AppLogger
import com.dd.dual.space.features.workspace.domain.GameLaunchResult
import com.dd.dual.space.features.workspace.domain.GameLaunchReadiness
import com.dd.dual.space.features.workspace.domain.GameLauncher
import com.dd.dual.space.features.workspace.domain.GameSession
import com.dd.dual.space.features.workspace.domain.LaunchUnavailableReason
import com.dd.dual.space.features.workspace.domain.ProfileTarget

class AndroidProfileGameLauncher(
    context: Context,
    private val profileResolver: AndroidManagedProfileResolver = AndroidManagedProfileResolver(context),
) : GameLauncher {
    private val launcherApps: LauncherApps = context.getSystemService(LauncherApps::class.java)

    override fun readiness(session: GameSession): GameLaunchReadiness {
        return try {
            val profile: UserHandle = profileResolver.resolve(session.profileTarget)
                ?: return GameLaunchReadiness.Unavailable(LaunchUnavailableReason.missingManagedProfile)
            val activity: LauncherActivityInfo? = launcherApps.getActivityList(session.packageName, profile).firstOrNull()
            if (activity == null) {
                GameLaunchReadiness.Unavailable(LaunchUnavailableReason.gameNotInstalled)
            } else {
                GameLaunchReadiness.Ready
            }
        } catch (error: Exception) {
            AppLogger.error("check_game_readiness", error, mapOf("sessionId" to session.id))
            GameLaunchReadiness.Unavailable(LaunchUnavailableReason.permissionDenied)
        }
    }

    override fun launch(session: GameSession): GameLaunchResult {
        AppLogger.action("launch_profile_game", mapOf("sessionId" to session.id, "profile" to session.profileTarget.name))
        val readiness: GameLaunchReadiness = readiness(session)
        if (readiness is GameLaunchReadiness.Unavailable) return unavailable(session, readiness.reason)
        val profile: UserHandle = profileResolver.resolve(session.profileTarget)
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

    private fun unavailable(session: GameSession, reason: LaunchUnavailableReason): GameLaunchResult.Unavailable {
        AppLogger.error("launch_profile_game_unavailable", IllegalStateException(reason.name), mapOf("sessionId" to session.id))
        return GameLaunchResult.Unavailable(reason)
    }
}
