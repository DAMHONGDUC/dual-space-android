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
import com.duplicateapp.gamespace.core.theme.game_space_dimensions
import com.duplicateapp.gamespace.features.workspace.domain.profile_provisioning_status

@Composable
fun settings_dialog(
    remaining_quota_hours: Int,
    profile_status: profile_provisioning_status,
    on_open_android_settings: () -> Unit,
    on_dismiss: () -> Unit,
) {
    val profile_label: String = stringResource(
        when (profile_status) {
            profile_provisioning_status.available -> R.string.profile_available
            profile_provisioning_status.already_created -> R.string.profile_count
            profile_provisioning_status.unsupported -> R.string.profile_unsupported
        },
    )
    AlertDialog(
        onDismissRequest = on_dismiss,
        title = { Text(stringResource(R.string.settings)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(game_space_dimensions.space_8)) {
                Text(pluralStringResource(R.plurals.settings_quota, remaining_quota_hours, remaining_quota_hours))
                Text(stringResource(R.string.settings_profile, profile_label))
                Text(stringResource(R.string.settings_privacy))
            }
        },
        confirmButton = {
            TextButton(onClick = on_open_android_settings) { Text(stringResource(R.string.android_settings)) }
        },
        dismissButton = { TextButton(onClick = on_dismiss) { Text(stringResource(R.string.close)) } },
    )
}

@Composable
fun delete_session_dialog(session_name: String, on_confirm: () -> Unit, on_dismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = on_dismiss,
        title = { Text(stringResource(R.string.delete_session)) },
        text = { Text(stringResource(R.string.delete_session_message, session_name)) },
        confirmButton = { TextButton(onClick = on_confirm) { Text(stringResource(R.string.delete)) } },
        dismissButton = { TextButton(onClick = on_dismiss) { Text(stringResource(R.string.cancel)) } },
    )
}
