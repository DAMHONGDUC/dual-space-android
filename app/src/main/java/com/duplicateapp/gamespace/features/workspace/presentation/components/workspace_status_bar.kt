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
import com.duplicateapp.gamespace.core.theme.GameSpaceDimensions
import com.duplicateapp.gamespace.features.workspace.domain.GameSession
import com.duplicateapp.gamespace.features.workspace.domain.SessionState

@Composable
fun workspaceStatusBar(session: GameSession) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(GameSpaceDimensions.space8),
        horizontalArrangement = Arrangement.spacedBy(GameSpaceDimensions.space4),
    ) {
        statusPill(stateLabel(session.state))
        metricPill(stringResource(R.string.metric_cpu), stringResource(R.string.metric_value_percent, session.cpuPercent))
        metricPill(stringResource(R.string.metric_memory), stringResource(R.string.metric_value_memory, session.memoryGb))
        metricPill(stringResource(R.string.metric_fps), stringResource(R.string.metric_value_fps, session.framesPerSecond))
        metricPill(
            stringResource(R.string.metric_temperature),
            stringResource(R.string.metric_value_temperature, session.temperatureCelsius),
        )
    }
}

@Composable
private fun statusPill(label: String) {
    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.large) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = GameSpaceDimensions.space8, vertical = GameSpaceDimensions.space4),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun metricPill(label: String, value: String) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.large) {
        Text(
            stringResource(R.string.metric_label_value, label, value),
            modifier = Modifier.padding(horizontal = GameSpaceDimensions.space8, vertical = GameSpaceDimensions.space4),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun stateLabel(state: SessionState): String {
    val resourceId: Int = when (state) {
        SessionState.starting -> R.string.session_starting
        SessionState.running -> R.string.session_running
        SessionState.paused -> R.string.session_paused
        SessionState.stopped -> R.string.session_stopped
        SessionState.failed -> R.string.session_failed
    }
    return stringResource(resourceId)
}
