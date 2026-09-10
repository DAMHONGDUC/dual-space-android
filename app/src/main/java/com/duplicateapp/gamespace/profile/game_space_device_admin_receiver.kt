package com.duplicateapp.gamespace.profile

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.duplicateapp.gamespace.R
import com.duplicateapp.gamespace.core.logging.app_logger

class game_space_device_admin_receiver : DeviceAdminReceiver() {
    override fun on_profile_provisioning_complete(context: Context, intent: Intent) {
        val manager: DevicePolicyManager = context.getSystemService(DevicePolicyManager::class.java)
        val admin = ComponentName(context, game_space_device_admin_receiver::class.java)
        app_logger.action("complete_profile_provisioning", emptyMap())
        try {
            manager.setProfileName(admin, context.getString(R.string.game_profile_name))
            manager.setProfileEnabled(admin)
            app_logger.success("complete_profile_provisioning", mapOf("profile" to "managed"))
        } catch (error: Exception) {
            app_logger.error("complete_profile_provisioning", error, emptyMap())
        }
    }
}
