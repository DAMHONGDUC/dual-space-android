package com.dd.dual.space.features.workspace.presentation.components

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
import com.dd.dual.space.R
import com.dd.dual.space.core.theme.ParallelAppDimensions
import com.dd.dual.space.features.workspace.domain.GameSession
import com.dd.dual.space.features.workspace.domain.ProfileTarget

@Composable
fun sessionDropdown(
    sessions: List<GameSession>,
    selectedSessionId: String?,
    onSessionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded: Boolean by remember { mutableStateOf(false) }
    val selectedSession: GameSession? = sessions.firstOrNull { session -> session.id == selectedSessionId }
    val selectedSpace: String? = selectedSession?.let { session -> sessionSpaceLabel(session) }

    Column(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = sessions.isNotEmpty()) { expanded = true },
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = ParallelAppDimensions.space12, vertical = ParallelAppDimensions.space6),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (selectedSession == null || selectedSpace == null) {
                        stringResource(R.string.no_game_selected)
                    } else {
                        stringResource(R.string.session_summary, selectedSession.gameName, selectedSpace)
                    },
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                )
                Icon(
                    Icons.Filled.ArrowDropDown,
                    contentDescription = stringResource(R.string.choose_session),
                    modifier = Modifier.size(ParallelAppDimensions.iconSmall),
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.widthIn(
                min = ParallelAppDimensions.dropdownMenuMinWidth,
                max = ParallelAppDimensions.dropdownMenuMaxWidth,
            ),
        ) {
            sessions.forEach { session ->
                val selected: Boolean = session.id == selectedSessionId
                val space: String = sessionSpaceLabel(session)
                DropdownMenuItem(
                    leadingIcon = { RadioButton(selected = selected, onClick = null) },
                    text = {
                        Text(
                            text = stringResource(R.string.session_summary, session.gameName, space),
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                        )
                    },
                    onClick = {
                        onSessionSelected(session.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun sessionSpaceLabel(session: GameSession): String =
    if (session.profileTarget == ProfileTarget.personal) {
        stringResource(R.string.main_profile)
    } else {
        stringResource(R.string.copy_number, session.virtualUserId)
    }
