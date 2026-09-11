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
import com.duplicateapp.gamespace.core.theme.ParallelAppDimensions
import com.duplicateapp.gamespace.features.workspace.domain.GameSession
import android.text.format.DateUtils
@Composable
fun workspaceStatusBar(session: GameSession) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(ParallelAppDimensions.space8),
        horizontalArrangement = Arrangement.spacedBy(ParallelAppDimensions.space4),
    ) {
        statusPill(stringResource(R.string.ready_to_open))
        session.lastOpenedAtEpochMillis?.let { openedAt ->
            statusPill(stringResource(R.string.last_opened, DateUtils.getRelativeTimeSpanString(openedAt)))
        }
    }
}

@Composable
private fun statusPill(label: String) {
    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.large) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = ParallelAppDimensions.space8, vertical = ParallelAppDimensions.space4),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}
