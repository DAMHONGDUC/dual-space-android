package com.dd.dual.space.features.workspace.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.dd.dual.space.R
import com.dd.dual.space.core.theme.ParallelAppDimensions
import com.dd.dual.space.features.workspace.domain.InstalledGame
import com.dd.dual.space.features.workspace.domain.filterByQuery

@Composable
fun addSessionDialog(
    games: List<InstalledGame>,
    onDismiss: () -> Unit,
    onAdd: (String, InstalledGame) -> Unit,
    onRefresh: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    var query: String by rememberSaveable { mutableStateOf("") }
    val visibleGames: List<InstalledGame> = remember(games, query) { games.filterByQuery(query) }
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
            Column(verticalArrangement = Arrangement.spacedBy(ParallelAppDimensions.space12)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { value -> query = value },
                    modifier = Modifier.fillMaxWidth().testTag(appSearchFieldTestTag),
                    placeholder = { Text(stringResource(R.string.search_apps_hint)) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.clear_search))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(ParallelAppDimensions.itemCornerRadius),
                )
                LazyColumn(
                    modifier = Modifier.heightIn(max = ParallelAppDimensions.dialogListMaxHeightExpanded),
                    verticalArrangement = Arrangement.spacedBy(ParallelAppDimensions.space8),
                ) {
                    items(visibleGames, key = InstalledGame::packageName) { game ->
                        Surface(
                            onClick = { onAdd(game.label, game) },
                            enabled = game.isCopyAvailable,
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
                                        imageVector = Icons.Filled.Apps,
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
                                    Text(
                                        text = stringResource(
                                            if (game.isCopyAvailable) R.string.copy_ready else R.string.copy_install_required,
                                        ),
                                        color = if (game.isCopyAvailable) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.labelSmall,
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
                    if (visibleGames.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(vertical = ParallelAppDimensions.space24),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(ParallelAppDimensions.space8),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Apps,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(ParallelAppDimensions.dialogIconContainer),
                                )
                                Text(
                                    text = if (games.isEmpty()) stringResource(R.string.no_apps_in_profile)
                                    else stringResource(R.string.search_no_results, query.trim()),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onRefresh) { Text(stringResource(R.string.rescan)) } },
        dismissButton = {
            Row {
                TextButton(onClick = onOpenSettings) { Text(stringResource(R.string.android_settings)) }
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
            }
        },
        shape = RoundedCornerShape(ParallelAppDimensions.dialogCornerRadius),
    )
}

internal const val appSearchFieldTestTag: String = "app-search-field"
