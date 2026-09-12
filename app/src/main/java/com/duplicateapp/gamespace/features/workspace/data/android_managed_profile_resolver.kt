package com.duplicateapp.gamespace.features.workspace.data

import android.content.Context
import android.content.pm.LauncherApps
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import com.duplicateapp.gamespace.core.logging.AppLogger
import com.duplicateapp.gamespace.features.workspace.domain.ProfileTarget

class AndroidManagedProfileResolver(context: Context) {
    private val launcherApps: LauncherApps = context.getSystemService(LauncherApps::class.java)
    private val userManager: UserManager = context.getSystemService(UserManager::class.java)
    private val hostPackageName: String = context.packageName

    fun resolve(target: ProfileTarget): UserHandle? {
        val personalProfile: UserHandle = Process.myUserHandle()
        if (target == ProfileTarget.personal) return personalProfile
        return userManager.userProfiles.asSequence()
            .filter { profile -> profile != personalProfile }
            .firstOrNull(::containsHostApplication)
    }

    private fun containsHostApplication(profile: UserHandle): Boolean = try {
        launcherApps.getActivityList(hostPackageName, profile).isNotEmpty()
    } catch (error: Exception) {
        AppLogger.error("resolve_managed_profile", error, mapOf("profileHash" to profile.hashCode()))
        false
    }
}
