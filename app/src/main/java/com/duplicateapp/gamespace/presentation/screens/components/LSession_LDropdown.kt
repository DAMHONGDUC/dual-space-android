package com.duplicateapp.gamespace.features.workspace.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.duplicateapp.gamespace.R
import com.duplicateapp.gamespace.core.theme.game_space_dimensions
import com.duplicateapp.gamespace.features.workspace.domain.game_session
import com.duplicateapp.gamespace.features.workspace.domain.profile_target

@Composable
fun session_dropdown(
    sessions: List<game_session>,
    selected_session_id: String?,
    on_session_selected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded: Boolean by remember { mutableStateOf(false) }
    val selected_session: game_session? = sessions.firstOrNull { session -> session.id == selected_session_id }
    val selected_space: String? = selected_session?.let { session ->
        stringResource(if (session.profile_target == profile_target.personal) R.string.main_profile else R.string.work_profile)
    }

    Column(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = sessions.isNotEmpty()) { expanded = true },
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = game_space_dimensions.space_12, vertical = game_space_dimensions.space_6),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (selected_session == null || selected_space == null) {
                        stringResource(R.string.no_game_selected)
                    } else {
                        stringResource(R.string.session_summary, selected_session.game_name, selected_space)
                    },
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                )
                Icon(
                    Icons.Filled.ArrowDropDown,
                    contentDescription = stringResource(R.string.choose_session),
                    modifier = Modifier.size(game_space_dimensions.icon_small),
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.widthIn(
                min = game_space_dimensions.dropdown_menu_min_width,
                max = game_space_dimensions.dropdown_menu_max_width,
            ),
        ) {
            sessions.forEach { session ->
                val selected: Boolean = session.id == selected_session_id
                val space: String = stringResource(
                    if (session.profile_target == profile_target.personal) R.string.main_profile else R.string.work_profile,
                )
                DropdownMenuItem(
                    leadingIcon = { RadioButton(selected = selected, onClick = null) },
                    text = {
                        Text(
                            text = stringResource(R.string.session_summary, session.game_name, space),
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                        )
                    },
                    onClick = {
                        on_session_selected(session.id)
                        expanded = false
                    },
                )
            }
        }
    }
}
