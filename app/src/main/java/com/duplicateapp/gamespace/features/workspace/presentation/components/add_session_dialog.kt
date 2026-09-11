package com.duplicateapp.gamespace.features.workspace.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.duplicateapp.gamespace.R
import com.duplicateapp.gamespace.core.theme.ParallelAppDimensions
import com.duplicateapp.gamespace.features.workspace.domain.InstalledGame

@Composable
fun addSessionDialog(
    games: List<InstalledGame>,
    onDismiss: () -> Unit,
    onAdd: (String, InstalledGame) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(ParallelAppDimensions.itemCornerRadius),
            ) {
                Icon(
                    imageVector = Icons.Filled.ContentCopy,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(ParallelAppDimensions.space12),
                )
            }
        },
        title = {
            Text(
                text = stringResource(R.string.choose_game_to_copy),
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = ParallelAppDimensions.dialogListMaxHeightExpanded),
                verticalArrangement = Arrangement.spacedBy(ParallelAppDimensions.space8),
            ) {
                    items(games, key = InstalledGame::packageName) { game ->
                        Surface(
                            onClick = { onAdd(game.label, game) },
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(ParallelAppDimensions.itemCornerRadius),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(ParallelAppDimensions.space12),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(ParallelAppDimensions.space12),
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(ParallelAppDimensions.space12),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.SportsEsports,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(ParallelAppDimensions.space12),
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(game.label, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        game.packageName,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Filled.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    if (games.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(vertical = ParallelAppDimensions.space24),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(ParallelAppDimensions.space8),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SportsEsports,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(ParallelAppDimensions.dialogIconContainer),
                                )
                                Text(
                                    text = stringResource(R.string.no_apps_in_profile),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
        shape = RoundedCornerShape(ParallelAppDimensions.dialogCornerRadius),
    )
}
