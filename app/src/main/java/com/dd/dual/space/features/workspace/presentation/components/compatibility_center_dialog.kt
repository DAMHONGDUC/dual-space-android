package com.dd.dual.space.features.workspace.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dd.dual.space.R
import com.dd.dual.space.core.theme.ParallelAppDimensions
import com.dd.dual.space.features.workspace.domain.GameLaunchReadiness
import com.dd.dual.space.features.workspace.domain.GameSession
import com.dd.dual.space.features.workspace.domain.LaunchUnavailableReason

@Composable
fun compatibilityCenterDialog(
    sessions: List<GameSession>,
    readinessBySessionId: Map<String, GameLaunchReadiness>,
    onShareReport: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { dialogIcon(Icons.Filled.HealthAndSafety) },
        title = { Text(stringResource(R.string.compatibility_center), style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(ParallelAppDimensions.space8),
            ) {
                if (sessions.isEmpty()) Text(stringResource(R.string.compatibility_empty))
                sessions.forEach { session ->
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(ParallelAppDimensions.space12),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(session.name, style = MaterialTheme.typography.labelLarge)
                                Text(session.gameName, style = MaterialTheme.typography.bodySmall)
                            }
                            Text(readinessLabel(readinessBySessionId[session.id]), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onShareReport) { Text(stringResource(R.string.share_diagnostic_report)) } },
        dismissButton = {
            Row {
                TextButton(onClick = onOpenSettings) { Text(stringResource(R.string.android_settings)) }
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
            }
        },
    )
}

@Composable
private fun readinessLabel(readiness: GameLaunchReadiness?): String = stringResource(
    when (readiness) {
        GameLaunchReadiness.Ready -> R.string.ready_to_open
        is GameLaunchReadiness.Unavailable -> when (readiness.reason) {
            LaunchUnavailableReason.missingManagedProfile -> R.string.compatibility_profile_missing
            LaunchUnavailableReason.gameNotInstalled -> R.string.compatibility_game_missing
            LaunchUnavailableReason.permissionDenied -> R.string.compatibility_permission
            LaunchUnavailableReason.engineUnavailable -> R.string.launch_engine_unavailable
            LaunchUnavailableReason.installFailed -> R.string.copy_install_failed
            LaunchUnavailableReason.launchTimedOut -> R.string.launch_timed_out
        }
        null -> R.string.compatibility_checking
    },
)
