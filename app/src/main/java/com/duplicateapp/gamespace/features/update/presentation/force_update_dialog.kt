package com.duplicateapp.gamespace.features.update.presentation

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.duplicateapp.gamespace.R

@Composable
fun forceUpdateDialog(onUpdate: () -> Unit, onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(stringResource(R.string.update_required_title)) },
        text = { Text(stringResource(R.string.update_required_message)) },
        confirmButton = { Button(onClick = onUpdate) { Text(stringResource(R.string.update_now)) } },
        dismissButton = { TextButton(onClick = onClose) { Text(stringResource(R.string.close_app)) } },
    )
}
