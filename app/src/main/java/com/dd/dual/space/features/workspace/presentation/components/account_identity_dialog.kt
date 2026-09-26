package com.dd.dual.space.features.workspace.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dd.dual.space.R
import com.dd.dual.space.core.theme.ParallelAppDimensions
import com.dd.dual.space.features.workspace.domain.AccountColor
import com.dd.dual.space.features.workspace.domain.GameSession

@Composable
fun accountIdentityDialog(
    session: GameSession,
    onSave: (String, AccountColor) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    var name: String by remember(session.id) { mutableStateOf(session.name) }
    var accountColor: AccountColor by remember(session.id) { mutableStateOf(session.accountColor) }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { dialogIcon(Icons.Filled.Edit) },
        title = { Text(stringResource(R.string.edit_account), style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(ParallelAppDimensions.space12)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { value -> name = value },
                    label = { Text(stringResource(R.string.session_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(stringResource(R.string.account_color), style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(ParallelAppDimensions.space8),
                    verticalArrangement = Arrangement.spacedBy(ParallelAppDimensions.space8),
                ) {
                    AccountColor.entries.forEach { color ->
                        FilterChip(
                            selected = accountColor == color,
                            onClick = { accountColor = color },
                            label = { Text(accountColorLabel(color)) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, accountColor) }, enabled = name.isNotBlank()) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDelete) { Text(stringResource(R.string.delete)) }
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

@Composable
private fun accountColorLabel(accountColor: AccountColor): String = stringResource(
    when (accountColor) {
        AccountColor.blue -> R.string.color_blue
        AccountColor.green -> R.string.color_green
        AccountColor.orange -> R.string.color_orange
        AccountColor.purple -> R.string.color_purple
    },
)
