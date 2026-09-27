package com.dd.dual.space.features.workspace.data

import android.content.Context
import android.content.Intent
import android.os.Build
import com.dd.dual.space.BuildConfig
import com.dd.dual.space.R
import com.dd.dual.space.core.logging.AppLogger
import com.dd.dual.space.features.workspace.domain.DiagnosticReporter
import com.dd.dual.space.features.workspace.domain.GameLaunchReadiness
import com.dd.dual.space.features.workspace.domain.ProfileTarget

class AndroidDiagnosticReporter(private val context: Context) : DiagnosticReporter {
    override fun share(profileTarget: ProfileTarget, readiness: GameLaunchReadiness?) {
        AppLogger.action("share_diagnostic_report", mapOf("profile" to profileTarget.name))
        val readinessName: String = when (readiness) {
            GameLaunchReadiness.Ready -> "ready"
            is GameLaunchReadiness.Unavailable -> readiness.reason.name
            null -> "not_checked"
        }
        val report: String = context.getString(
            R.string.diagnostic_report_body,
            BuildConfig.VERSION_NAME,
            Build.VERSION.SDK_INT,
            Build.MANUFACTURER,
            Build.MODEL,
            profileTarget.name,
            readinessName,
        )
        val shareIntent: Intent = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.diagnostic_report_title))
            .putExtra(Intent.EXTRA_TEXT, report)
        try {
            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_diagnostic_report)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            AppLogger.success("share_diagnostic_report", mapOf("profile" to profileTarget.name))
        } catch (error: Exception) {
            AppLogger.error("share_diagnostic_report", error, mapOf("profile" to profileTarget.name))
        }
    }
}
