package com.duplicateapp.gamespace.features.workspace.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import com.duplicateapp.gamespace.R
import com.duplicateapp.gamespace.core.theme.ParallelAppDimensions
import com.duplicateapp.gamespace.features.workspace.domain.ProfileProvisioningStatus

@Composable
fun settingsDialog(
    remainingQuotaHours: Int,
    profileStatus: ProfileProvisioningStatus,
    onOpenAndroidSettings: () -> Unit,
    onDismiss: () -> Unit,
) {
    val profileLabel: String = stringResource(
        when (profileStatus) {
            ProfileProvisioningStatus.available -> R.string.profile_available
            ProfileProvisioningStatus.alreadyCreated -> R.string.profile_count
            ProfileProvisioningStatus.unsupported -> R.string.profile_unsupported
        },
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(ParallelAppDimensions.space8)) {
                Text(pluralStringResource(R.plurals.settings_quota, remainingQuotaHours, remainingQuotaHours))
                Text(stringResource(R.string.settings_profile, profileLabel))
                Text(stringResource(R.string.settings_privacy))
            }
        },
        confirmButton = {
            TextButton(onClick = onOpenAndroidSettings) { Text(stringResource(R.string.android_settings)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) } },
    )
}

@Composable
fun deleteSessionDialog(sessionName: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_session)) },
        text = { Text(stringResource(R.string.delete_session_message, sessionName)) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.delete)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}
