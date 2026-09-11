package com.duplicateapp.gamespace.features.workspace.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.duplicateapp.gamespace.R
import com.duplicateapp.gamespace.core.theme.ParallelAppDimensions
import com.duplicateapp.gamespace.features.workspace.domain.InstalledGame
import com.duplicateapp.gamespace.features.workspace.domain.ProfileTarget

@Composable
fun addSessionDialog(
    games: List<InstalledGame>,
    onDismiss: () -> Unit,
    onProfileChange: (ProfileTarget) -> Unit,
    onAdd: (String, InstalledGame) -> Unit,
) {
    var selectedProfile: ProfileTarget by rememberSaveable { mutableStateOf(ProfileTarget.personal) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.choose_game_to_copy)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(ParallelAppDimensions.space12)) {
                Row(horizontalArrangement = Arrangement.spacedBy(ParallelAppDimensions.space8)) {
                    ProfileTarget.entries.forEach { profileTarget ->
                        FilterChip(
                            selected = selectedProfile == profileTarget,
                            onClick = {
                                selectedProfile = profileTarget
                                onProfileChange(profileTarget)
                            },
                            label = {
                                Text(
                                    stringResource(
                                        if (profileTarget == ProfileTarget.personal) R.string.main_profile
                                        else R.string.work_profile,
                                    ),
                                )
                            },
                        )
                    }
                }
                LazyColumn(modifier = Modifier.heightIn(max = ParallelAppDimensions.dialogListMaxHeightExpanded)) {
                    items(games, key = InstalledGame::packageName) { game ->
                        Column(
                            modifier = Modifier.fillMaxWidth().clickable { onAdd(game.label, game) }
                                .padding(vertical = ParallelAppDimensions.space12),
                        ) {
                            Text(game.label, style = MaterialTheme.typography.titleSmall)
                            Text(game.packageName, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    if (games.isEmpty()) item { Text(stringResource(R.string.no_apps_in_profile)) }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}
