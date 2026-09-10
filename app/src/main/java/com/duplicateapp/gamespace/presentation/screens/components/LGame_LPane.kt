package com.duplicateapp.gamespace.features.workspace.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.duplicateapp.gamespace.R
import com.duplicateapp.gamespace.core.theme.game_space_colors
import com.duplicateapp.gamespace.core.theme.game_space_dimensions
import com.duplicateapp.gamespace.features.workspace.domain.game_session
import com.duplicateapp.gamespace.features.workspace.domain.session_state

@Composable
fun empty_game_pane(on_add_game: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.padding(
            start = game_space_dimensions.space_8,
            end = game_space_dimensions.space_8,
            top = game_space_dimensions.space_4,
            bottom = game_space_dimensions.space_8,
        ),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(game_space_dimensions.space_8, Alignment.CenterVertically),
        ) {
            Icon(Icons.Filled.SportsEsports, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(stringResource(R.string.no_game_selected), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.select_game_description), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = on_add_game) { Text(stringResource(R.string.add_game)) }
        }
    }
}

@Composable
fun game_pane(
    session: game_session,
    on_toggle: () -> Unit,
    on_delete: () -> Unit,
    can_delete: Boolean,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    Surface(
        modifier = modifier.padding(
            start = game_space_dimensions.space_8,
            end = game_space_dimensions.space_8,
            top = game_space_dimensions.space_4,
            bottom = game_space_dimensions.space_8,
        ),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            session_toolbar(session = session, compact = compact, can_delete = can_delete, on_delete = on_delete)
            game_stage(
                modifier = Modifier.weight(1f),
                game_name = session.game_name,
                compact = compact,
            )
            control_dock(on_launch = on_toggle)
        }
    }
}

@Composable
private fun session_toolbar(session: game_session, compact: Boolean, can_delete: Boolean, on_delete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = game_space_dimensions.space_8),
        horizontalArrangement = Arrangement.spacedBy(game_space_dimensions.space_4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.weight(1f))
        if (!compact) {
            metric_text(stringResource(R.string.metric_cpu), stringResource(R.string.metric_value_percent, session.cpu_percent))
            metric_text(stringResource(R.string.metric_fps), stringResource(R.string.metric_value_fps, session.frames_per_second))
        }
        state_badge(session.state)
        IconButton(onClick = on_delete, enabled = can_delete, modifier = Modifier.size(game_space_dimensions.touch_target)) {
            Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.delete_session))
        }
    }
}

@Composable
private fun game_stage(modifier: Modifier, game_name: String, compact: Boolean) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = game_space_dimensions.space_8)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.background)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f), MaterialTheme.shapes.medium),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(game_space_dimensions.space_4),
        ) {
            Box(
                modifier = Modifier
                    .size(
                        if (compact) game_space_dimensions.stage_icon_compact else game_space_dimensions.stage_icon,
                    )
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.SportsEsports, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Text(game_name, style = MaterialTheme.typography.titleSmall)
            if (!compact) {
                Text(
                    stringResource(R.string.game_stage_waiting),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun control_dock(on_launch: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(game_space_dimensions.space_8),
        horizontalArrangement = Arrangement.spacedBy(game_space_dimensions.space_8, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = on_launch) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null)
            Text(stringResource(R.string.open_game))
        }
    }
}

@Composable
private fun metric_text(label: String, value: String) {
    Text(
        stringResource(R.string.metric_compact, label, value),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelMedium,
    )
}

@Composable
private fun state_badge(state: session_state) {
    val (label, color) = when (state) {
        session_state.starting -> stringResource(R.string.session_starting) to game_space_colors.warning
        session_state.running -> stringResource(R.string.session_running) to game_space_colors.success
        session_state.paused -> stringResource(R.string.session_paused) to MaterialTheme.colorScheme.primary
        session_state.stopped -> stringResource(R.string.session_stopped) to MaterialTheme.colorScheme.onSurfaceVariant
        session_state.failed -> stringResource(R.string.session_failed) to MaterialTheme.colorScheme.error
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(game_space_dimensions.space_4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(game_space_dimensions.status_dot).clip(CircleShape).background(color))
        Text(label, color = color, style = MaterialTheme.typography.labelMedium)
    }
}
