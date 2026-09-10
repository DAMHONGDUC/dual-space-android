package com.duplicateapp.gamespace.profile

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.os.Bundle
import com.duplicateapp.gamespace.R
import com.duplicateapp.gamespace.core.logging.app_logger

class policy_compliance_activity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val manager: DevicePolicyManager = getSystemService(DevicePolicyManager::class.java)
        val admin = ComponentName(this, game_space_device_admin_receiver::class.java)
        app_logger.action("apply_profile_compliance", emptyMap())
        try {
            manager.setProfileName(admin, getString(R.string.game_profile_name))
            manager.setProfileEnabled(admin)
            app_logger.success("apply_profile_compliance", mapOf("profile" to "managed"))
        } catch (error: Exception) {
            app_logger.error("apply_profile_compliance", error, emptyMap())
        }
        setResult(RESULT_OK)
        finish()
    }
}
