package com.duplicateapp.gamespace.features.workspace.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.duplicateapp.gamespace.R
import com.duplicateapp.gamespace.core.theme.game_space_dimensions
import com.duplicateapp.gamespace.features.workspace.domain.game_session
import com.duplicateapp.gamespace.features.workspace.domain.session_state

@Composable
fun workspace_status_bar(session: game_session) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(game_space_dimensions.space_8),
        horizontalArrangement = Arrangement.spacedBy(game_space_dimensions.space_4),
    ) {
        status_pill(state_label(session.state))
        metric_pill(stringResource(R.string.metric_cpu), stringResource(R.string.metric_value_percent, session.cpu_percent))
        metric_pill(stringResource(R.string.metric_memory), stringResource(R.string.metric_value_memory, session.memory_gb))
        metric_pill(stringResource(R.string.metric_fps), stringResource(R.string.metric_value_fps, session.frames_per_second))
        metric_pill(
            stringResource(R.string.metric_temperature),
            stringResource(R.string.metric_value_temperature, session.temperature_celsius),
        )
    }
}

@Composable
private fun status_pill(label: String) {
    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.large) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = game_space_dimensions.space_8, vertical = game_space_dimensions.space_4),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun metric_pill(label: String, value: String) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.large) {
        Text(
            stringResource(R.string.metric_label_value, label, value),
            modifier = Modifier.padding(horizontal = game_space_dimensions.space_8, vertical = game_space_dimensions.space_4),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun state_label(state: session_state): String {
    val resource_id: Int = when (state) {
        session_state.starting -> R.string.session_starting
        session_state.running -> R.string.session_running
        session_state.paused -> R.string.session_paused
        session_state.stopped -> R.string.session_stopped
        session_state.failed -> R.string.session_failed
    }
    return stringResource(resource_id)
}
