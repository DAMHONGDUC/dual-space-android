package com.duplicateapp.gamespace.features.workspace.data

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Process
import android.os.UserManager
import com.duplicateapp.gamespace.core.logging.AppLogger
import com.duplicateapp.gamespace.features.workspace.domain.ProfileProvisioner
import com.duplicateapp.gamespace.features.workspace.domain.ProfileProvisioningStatus
import com.duplicateapp.gamespace.profile.ParallelAppDeviceAdminReceiver

class AndroidProfileProvisioner(private val context: Context) : ProfileProvisioner {
    private val devicePolicyManager: DevicePolicyManager = context.getSystemService(DevicePolicyManager::class.java)
    private val userManager: UserManager = context.getSystemService(UserManager::class.java)

    override fun status(): ProfileProvisioningStatus {
        val hasAdditionalProfile: Boolean = userManager.userProfiles.any { profile -> profile != Process.myUserHandle() }
        return when {
            hasAdditionalProfile -> ProfileProvisioningStatus.alreadyCreated
            devicePolicyManager.isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE) -> {
                ProfileProvisioningStatus.available
            }
            else -> ProfileProvisioningStatus.unsupported
        }
    }

    override fun requestCreation(): Boolean {
        val currentStatus: ProfileProvisioningStatus = status()
        AppLogger.action("request_profile_creation", mapOf("status" to currentStatus.name))
        if (currentStatus != ProfileProvisioningStatus.available) return false

        return try {
            val admin: ComponentName = ComponentName(context, ParallelAppDeviceAdminReceiver::class.java)
            val intent = Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE).apply {
                putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME, admin)
            }
            val activity: Activity = context as? Activity ?: return false
            activity.startActivityForResult(intent, provisioningRequestCode)
            AppLogger.success("request_profile_creation", mapOf("result" to "consent_started"))
            true
        } catch (error: Exception) {
            AppLogger.error("request_profile_creation", error, emptyMap())
            false
        }
    }

    private companion object {
        const val provisioningRequestCode = 4101
    }
}
