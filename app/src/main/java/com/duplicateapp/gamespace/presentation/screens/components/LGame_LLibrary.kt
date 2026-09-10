package com.duplicateapp.gamespace.features.workspace.presentation.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.graphics.drawable.toBitmap
import com.duplicateapp.gamespace.R
import com.duplicateapp.gamespace.core.logging.app_logger
import com.duplicateapp.gamespace.core.theme.game_space_dimensions
import com.duplicateapp.gamespace.features.workspace.domain.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun game_library(
    sessions: List<game_session>,
    remaining_quota_hours: Int,
    content_padding: PaddingValues,
    on_launch: (String) -> Unit,
    on_add: () -> Unit,
    on_delete: (String) -> Unit,
    on_settings: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize().padding(content_padding)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.app_name), fontWeight = FontWeight.SemiBold)
                        Text(
                            stringResource(R.string.copy_count, sessions.size),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    Text(
                        stringResource(R.string.quota_hours_compact, remaining_quota_hours),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    IconButton(onClick = on_settings) {
                        Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.settings))
                    }
                },
            )
            if (sessions.isEmpty()) {
                empty_library(on_add, Modifier.weight(1f))
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(game_space_dimensions.game_tile_min_width),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = game_space_dimensions.space_16,
                        top = game_space_dimensions.space_12,
                        end = game_space_dimensions.space_16,
                        bottom = game_space_dimensions.fab_clearance,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(game_space_dimensions.space_12),
                    verticalArrangement = Arrangement.spacedBy(game_space_dimensions.space_12),
                ) {
                    items(sessions, key = game_session::id) { session -> game_tile(session, on_launch, on_delete) }
                }
            }
        }
        FloatingActionButton(
            onClick = on_add,
            modifier = Modifier.align(Alignment.BottomEnd).padding(game_space_dimensions.space_16),
        ) { Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_game)) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun game_tile(session: game_session, on_launch: (String) -> Unit, on_delete: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(
            onClick = { on_launch(session.id) },
            onLongClick = { on_delete(session.id) },
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(game_space_dimensions.space_12),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(game_space_dimensions.space_8),
        ) {
            game_icon(session.package_name)
            Text(
                session.game_name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(game_space_dimensions.space_4),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                session_status(session.state)
                Text(
                    stringResource(if (session.profile_target == profile_target.personal) R.string.original_copy else R.string.copy_one),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun game_icon(package_name: String) {
    val context = LocalContext.current
    val bitmap: ImageBitmap? = remember(package_name) {
        try {
            val drawable: Drawable = context.packageManager.getApplicationIcon(package_name)
            drawable.toBitmap(width = 144, height = 144).asImageBitmap()
        } catch (error: Exception) {
            app_logger.error("load_game_icon", error, mapOf("packageName" to package_name))
            null
        }
    }
    Surface(
        modifier = Modifier.size(game_space_dimensions.game_icon).clip(RoundedCornerShape(game_space_dimensions.icon_corner_radius)),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        if (bitmap == null) {
            Icon(Icons.Filled.SportsEsports, null, modifier = Modifier.padding(game_space_dimensions.space_16))
        } else {
            Image(BitmapPainter(bitmap), null, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun session_status(state: session_state) {
    val label = stringResource(
        when (state) {
            session_state.starting -> R.string.session_starting
            session_state.running -> R.string.session_running
            session_state.paused -> R.string.session_paused
            session_state.stopped -> R.string.session_stopped
            session_state.failed -> R.string.session_failed
        },
    )
    Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun empty_library(on_add: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(game_space_dimensions.space_16),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(game_space_dimensions.space_8, Alignment.CenterVertically),
    ) {
        Icon(Icons.Filled.SportsEsports, null, Modifier.size(game_space_dimensions.empty_icon), MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.empty_library_title), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.empty_library_description), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Button(onClick = on_add) {
            Icon(Icons.Filled.Add, null)
            Text(stringResource(R.string.add_game))
        }
    }
}
