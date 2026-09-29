package com.dd.dual.space.features.workspace.presentation.components

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
import androidx.compose.material.icons.filled.Apps
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
import com.dd.dual.space.R
import com.dd.dual.space.core.theme.ParallelAppDimensions
import com.dd.dual.space.features.workspace.domain.GameSession

@Composable
fun emptyGamePane(onAddGame: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.padding(
            start = ParallelAppDimensions.space8,
            end = ParallelAppDimensions.space8,
            top = ParallelAppDimensions.space4,
            bottom = ParallelAppDimensions.space8,
        ),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ParallelAppDimensions.space8, Alignment.CenterVertically),
        ) {
            Icon(Icons.Filled.Apps, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(stringResource(R.string.no_game_selected), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.select_game_description), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onAddGame) { Text(stringResource(R.string.add_game)) }
        }
    }
}

@Composable
fun gamePane(
    session: GameSession,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    canDelete: Boolean,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    Surface(
        modifier = modifier.padding(
            start = ParallelAppDimensions.space8,
            end = ParallelAppDimensions.space8,
            top = ParallelAppDimensions.space4,
            bottom = ParallelAppDimensions.space8,
        ),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            sessionToolbar(session = session, compact = compact, canDelete = canDelete, onDelete = onDelete)
            gameStage(
                modifier = Modifier.weight(1f),
                gameName = session.gameName,
                compact = compact,
            )
            controlDock(onLaunch = onToggle)
        }
    }
}

@Composable
private fun sessionToolbar(session: GameSession, compact: Boolean, canDelete: Boolean, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ParallelAppDimensions.space8),
        horizontalArrangement = Arrangement.spacedBy(ParallelAppDimensions.space4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.weight(1f))
        if (!compact) {
            Text(session.name, style = MaterialTheme.typography.labelMedium)
        }
        IconButton(onClick = onDelete, enabled = canDelete, modifier = Modifier.size(ParallelAppDimensions.touchTarget)) {
            Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.delete_session))
        }
    }
}

@Composable
private fun gameStage(modifier: Modifier, gameName: String, compact: Boolean) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ParallelAppDimensions.space8)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.background)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f), MaterialTheme.shapes.medium),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ParallelAppDimensions.space4),
        ) {
            Box(
                modifier = Modifier
                    .size(
                        if (compact) ParallelAppDimensions.stageIconCompact else ParallelAppDimensions.stageIcon,
                    )
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Apps, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Text(gameName, style = MaterialTheme.typography.titleSmall)
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
private fun controlDock(onLaunch: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(ParallelAppDimensions.space8),
        horizontalArrangement = Arrangement.spacedBy(ParallelAppDimensions.space8, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onLaunch) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null)
            Text(stringResource(R.string.open_game))
        }
    }
}
