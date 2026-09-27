package com.dd.dual.space.features.workspace.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dd.dual.space.R
import com.dd.dual.space.features.workspace.domain.GameLaunchReadiness
import com.dd.dual.space.features.workspace.domain.GameSession
import com.dd.dual.space.features.workspace.domain.LaunchUnavailableReason

@Composable
fun launchConfirmationDialog(
    session: GameSession,
    readiness: GameLaunchReadiness?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val unavailableReason: LaunchUnavailableReason? = (readiness as? GameLaunchReadiness.Unavailable)?.reason
    val messageResource: Int = when (unavailableReason) {
        LaunchUnavailableReason.missingManagedProfile -> R.string.missing_managed_profile
        LaunchUnavailableReason.gameNotInstalled -> R.string.game_not_installed
        LaunchUnavailableReason.permissionDenied -> R.string.profile_permission_denied
        LaunchUnavailableReason.engineUnavailable -> R.string.launch_engine_unavailable
        LaunchUnavailableReason.installFailed -> R.string.copy_install_failed
        LaunchUnavailableReason.launchTimedOut -> R.string.launch_timed_out
        null -> R.string.launch_confirmation_message
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { dialogIcon(Icons.Filled.PlayArrow) },
        title = { Text(stringResource(R.string.launch_confirmation_title, session.name), style = MaterialTheme.typography.headlineSmall) },
        text = {
            Text(
                if (unavailableReason == null) stringResource(messageResource, session.gameName) else stringResource(messageResource),
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        confirmButton = {
            if (unavailableReason == null) {
                TextButton(onClick = onConfirm) { Text(stringResource(R.string.open_game)) }
            } else {
                TextButton(onClick = onOpenSettings) { Text(stringResource(R.string.android_settings)) }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}
