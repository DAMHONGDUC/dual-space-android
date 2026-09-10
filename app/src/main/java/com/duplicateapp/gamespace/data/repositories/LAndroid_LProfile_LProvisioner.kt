package com.duplicateapp.gamespace.features.workspace.data

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Process
import android.os.UserManager
import com.duplicateapp.gamespace.core.logging.app_logger
import com.duplicateapp.gamespace.features.workspace.domain.profile_provisioner
import com.duplicateapp.gamespace.features.workspace.domain.profile_provisioning_status
import com.duplicateapp.gamespace.profile.game_space_device_admin_receiver

class android_profile_provisioner(private val context: Context) : profile_provisioner {
    private val device_policy_manager: DevicePolicyManager = context.getSystemService(DevicePolicyManager::class.java)
    private val user_manager: UserManager = context.getSystemService(UserManager::class.java)

    override fun status(): profile_provisioning_status {
        val has_additional_profile: Boolean = user_manager.userProfiles.any { profile -> profile != Process.myUserHandle() }
        return when {
            has_additional_profile -> profile_provisioning_status.already_created
            device_policy_manager.isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE) -> {
                profile_provisioning_status.available
            }
            else -> profile_provisioning_status.unsupported
        }
    }

    override fun request_creation(): Boolean {
        val current_status: profile_provisioning_status = status()
        app_logger.action("request_profile_creation", mapOf("status" to current_status.name))
        if (current_status != profile_provisioning_status.available) return false

        return try {
            val admin: ComponentName = ComponentName(context, game_space_device_admin_receiver::class.java)
            val intent = Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE).apply {
                putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME, admin)
            }
            val activity: Activity = context as? Activity ?: return false
            activity.startActivityForResult(intent, provisioning_request_code)
            app_logger.success("request_profile_creation", mapOf("result" to "consent_started"))
            true
        } catch (error: Exception) {
            app_logger.error("request_profile_creation", error, emptyMap())
            false
        }
    }

    private companion object {
        const val provisioning_request_code = 4101
    }
}
