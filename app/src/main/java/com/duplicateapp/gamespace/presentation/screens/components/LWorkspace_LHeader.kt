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
import com.duplicateapp.gamespace.core.theme.game_space_dimensions
import com.duplicateapp.gamespace.features.workspace.domain.game_session

@Composable
fun workspace_header(
    sessions: List<game_session>,
    selected_session_id: String?,
    remaining_quota_hours: Int,
    on_session_selected: (String) -> Unit,
    on_add_session: () -> Unit,
    on_settings: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = game_space_dimensions.space_12, vertical = game_space_dimensions.space_4),
        verticalArrangement = Arrangement.spacedBy(game_space_dimensions.space_4),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                stringResource(R.string.quota_remaining, remaining_quota_hours),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium,
            )
            IconButton(onClick = on_settings, modifier = Modifier.size(game_space_dimensions.touch_target)) {
                Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.settings))
            }
        }
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val dropdown_width = if (maxWidth >= game_space_dimensions.compact_breakpoint) {
                game_space_dimensions.dropdown_expanded_width
            } else {
                game_space_dimensions.dropdown_compact_width
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(game_space_dimensions.space_4),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                session_dropdown(
                    sessions = sessions,
                    selected_session_id = selected_session_id,
                    on_session_selected = on_session_selected,
                    modifier = Modifier.width(dropdown_width),
                )
                TextButton(onClick = on_add_session) {
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
