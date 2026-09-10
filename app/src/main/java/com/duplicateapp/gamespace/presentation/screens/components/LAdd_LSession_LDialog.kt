package com.duplicateapp.gamespace.features.workspace.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.duplicateapp.gamespace.R
import com.duplicateapp.gamespace.core.theme.game_space_dimensions
import com.duplicateapp.gamespace.features.workspace.domain.installed_game

@Composable
fun add_session_dialog(
    games: List<installed_game>,
    on_dismiss: () -> Unit,
    on_add: (String, installed_game) -> Unit,
) {
    AlertDialog(
        onDismissRequest = on_dismiss,
        title = { Text(stringResource(R.string.choose_game_to_copy)) },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = game_space_dimensions.dialog_list_max_height_expanded)) {
                items(games, key = installed_game::package_name) { game ->
                    Column(
                        modifier = Modifier.fillMaxWidth().clickable { on_add(game.label, game) }
                            .padding(vertical = game_space_dimensions.space_12),
                    ) {
                        Text(game.label, style = MaterialTheme.typography.titleSmall)
                        Text(game.package_name, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    }
                }
                if (games.isEmpty()) item { Text(stringResource(R.string.no_apps_in_profile)) }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = on_dismiss) { Text(stringResource(R.string.cancel)) } },
    )
}
