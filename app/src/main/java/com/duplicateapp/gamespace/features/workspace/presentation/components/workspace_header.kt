package com.duplicateapp.gamespace.features.workspace.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.duplicateapp.gamespace.R
import com.duplicateapp.gamespace.core.theme.ParallelAppDimensions
import com.duplicateapp.gamespace.features.workspace.domain.GameSession

@Composable
fun workspaceHeader(
    sessions: List<GameSession>,
    selectedSessionId: String?,
    remainingQuotaHours: Int,
    onSessionSelected: (String) -> Unit,
    onAddSession: () -> Unit,
    onSettings: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ParallelAppDimensions.space12, vertical = ParallelAppDimensions.space4),
        verticalArrangement = Arrangement.spacedBy(ParallelAppDimensions.space4),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                stringResource(R.string.quota_remaining, remainingQuotaHours),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium,
            )
            IconButton(onClick = onSettings, modifier = Modifier.size(ParallelAppDimensions.touchTarget)) {
                Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.settings))
            }
        }
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val dropdownWidth = if (maxWidth >= ParallelAppDimensions.compactBreakpoint) {
                ParallelAppDimensions.dropdownExpandedWidth
            } else {
                ParallelAppDimensions.dropdownCompactWidth
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(ParallelAppDimensions.space4),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                sessionDropdown(
                    sessions = sessions,
                    selectedSessionId = selectedSessionId,
                    onSessionSelected = onSessionSelected,
                    modifier = Modifier.width(dropdownWidth),
                )
                TextButton(onClick = onAddSession) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = null,
                    )
                    Text(stringResource(R.string.add_game))
                }
            }
        }
    }
}
