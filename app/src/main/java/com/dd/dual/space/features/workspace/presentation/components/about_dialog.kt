package com.dd.dual.space.features.workspace.presentation.components

import androidx.compose.foundation.layout.Arrangement
import com.dd.dual.space.features.workspace.domain.GameCopyLimits
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.dd.dual.space.R
import com.dd.dual.space.core.theme.ParallelAppDimensions

@Composable
fun aboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { dialogIcon(Icons.AutoMirrored.Outlined.HelpOutline) },
        title = { Text(stringResource(R.string.about_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(ParallelAppDimensions.space12),
            ) {
                Text(
                    stringResource(
                        R.string.about_description,
                        GameCopyLimits.maximumCopiesPerGame,
                        GameCopyLimits.maximumCopiesPerGame + 1,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                aboutStep(Icons.Filled.ContentCopy, R.string.about_step_copy)
                aboutStep(Icons.Filled.Shield, R.string.about_step_add)
                aboutStep(Icons.Filled.PlayArrow, R.string.about_step_play)
                Text(
                    stringResource(R.string.about_plan),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.got_it)) } },
        shape = RoundedCornerShape(ParallelAppDimensions.dialogCornerRadius),
    )
}

@Composable
private fun aboutStep(icon: ImageVector, textResId: Int) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(ParallelAppDimensions.itemCornerRadius),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(ParallelAppDimensions.space12),
            horizontalArrangement = Arrangement.spacedBy(ParallelAppDimensions.space12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(stringResource(textResId), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
