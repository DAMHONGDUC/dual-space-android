package com.dd.dual.space.features.update.data

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.dd.dual.space.core.logging.AppLogger
import com.dd.dual.space.features.update.domain.ForceUpdatePolicy
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

class ForceUpdateCoordinator(
    activity: Activity,
    private val launcher: ActivityResultLauncher<IntentSenderRequest>,
    private val onRequiredChanged: (Boolean) -> Unit,
) {
    private val updateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)
    private var pendingUpdate: AppUpdateInfo? = null

    fun check() {
        AppLogger.action("check_force_update", emptyMap())
        updateManager.appUpdateInfo
            .addOnSuccessListener { info ->
                val inProgress: Boolean = info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                val required: Boolean = inProgress || ForceUpdatePolicy.requiresUpdate(
                    isAvailable = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE,
                    isImmediateAllowed = info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE),
                    priority = info.updatePriority(),
                )
                pendingUpdate = info.takeIf { required }
                onRequiredChanged(required)
                AppLogger.success("check_force_update", mapOf("required" to required, "priority" to info.updatePriority()))
                if (inProgress) start()
            }
            .addOnFailureListener { error ->
                AppLogger.error("check_force_update", error, emptyMap())
                release()
            }
    }

    fun start() {
        val info: AppUpdateInfo = pendingUpdate ?: return check()
        AppLogger.action("start_force_update", mapOf("priority" to info.updatePriority()))
        try {
            val started: Boolean = updateManager.startUpdateFlowForResult(
                info,
                launcher,
                AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
            )
            if (!started) {
                AppLogger.error("start_force_update", IllegalStateException("flow_not_started"), emptyMap())
                release()
            }
        } catch (error: Exception) {
            AppLogger.error("start_force_update", error, emptyMap())
            release()
        }
    }

    // Fails open: when Play cannot check or start the update, the app stays usable instead of
    // blocking behind a dialog that can never complete; the next resume checks again.
    private fun release() {
        pendingUpdate = null
        onRequiredChanged(false)
    }
}
