package com.duplicateapp.gamespace.profile

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.os.Bundle
import com.duplicateapp.gamespace.R
import com.duplicateapp.gamespace.core.logging.AppLogger

class PolicyComplianceActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val manager: DevicePolicyManager = getSystemService(DevicePolicyManager::class.java)
        val admin = ComponentName(this, GameSpaceDeviceAdminReceiver::class.java)
        AppLogger.action("apply_profile_compliance", emptyMap())
        try {
            manager.setProfileName(admin, getString(R.string.game_profile_name))
            manager.setProfileEnabled(admin)
            AppLogger.success("apply_profile_compliance", mapOf("profile" to "managed"))
        } catch (error: Exception) {
            AppLogger.error("apply_profile_compliance", error, emptyMap())
        }
        setResult(RESULT_OK)
        finish()
    }
}
