package com.duplicateapp.gamespace.profile

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.duplicateapp.gamespace.R
import com.duplicateapp.gamespace.core.logging.AppLogger

class GameSpaceDeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        val manager: DevicePolicyManager = context.getSystemService(DevicePolicyManager::class.java)
        val admin = ComponentName(context, GameSpaceDeviceAdminReceiver::class.java)
        AppLogger.action("complete_profile_provisioning", emptyMap())
        try {
            manager.setProfileName(admin, context.getString(R.string.game_profile_name))
            manager.setProfileEnabled(admin)
            AppLogger.success("complete_profile_provisioning", mapOf("profile" to "managed"))
        } catch (error: Exception) {
            AppLogger.error("complete_profile_provisioning", error, emptyMap())
        }
    }
}
