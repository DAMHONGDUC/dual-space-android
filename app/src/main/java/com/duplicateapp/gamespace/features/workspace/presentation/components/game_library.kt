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
import com.duplicateapp.gamespace.core.logging.AppLogger
import com.duplicateapp.gamespace.core.theme.GameSpaceDimensions
import com.duplicateapp.gamespace.features.workspace.domain.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun gameLibrary(
    sessions: List<GameSession>,
    remainingQuotaHours: Int,
    contentPadding: PaddingValues,
    onLaunch: (String) -> Unit,
    onAdd: () -> Unit,
    onDelete: (String) -> Unit,
    onSettings: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
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
                        stringResource(R.string.quota_hours_compact, remainingQuotaHours),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.settings))
                    }
                },
            )
            if (sessions.isEmpty()) {
                emptyLibrary(onAdd, Modifier.weight(1f))
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(GameSpaceDimensions.gameTileMinWidth),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = GameSpaceDimensions.space16,
                        top = GameSpaceDimensions.space12,
                        end = GameSpaceDimensions.space16,
                        bottom = GameSpaceDimensions.fabClearance,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(GameSpaceDimensions.space12),
                    verticalArrangement = Arrangement.spacedBy(GameSpaceDimensions.space12),
                ) {
                    items(sessions, key = GameSession::id) { session -> gameTile(session, onLaunch, onDelete) }
                }
            }
        }
        FloatingActionButton(
            onClick = onAdd,
            modifier = Modifier.align(Alignment.BottomEnd).padding(GameSpaceDimensions.space16),
        ) { Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_game)) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun gameTile(session: GameSession, onLaunch: (String) -> Unit, onDelete: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(
            onClick = { onLaunch(session.id) },
            onLongClick = { onDelete(session.id) },
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(GameSpaceDimensions.space12),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(GameSpaceDimensions.space8),
        ) {
            gameIcon(session.packageName)
            Text(
                session.gameName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(GameSpaceDimensions.space4),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                sessionStatus(session.state)
                Text(
                    stringResource(if (session.profileTarget == ProfileTarget.personal) R.string.original_copy else R.string.copy_one),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun gameIcon(packageName: String) {
    val context = LocalContext.current
    val bitmap: ImageBitmap? = remember(packageName) {
        try {
            val drawable: Drawable = context.packageManager.getApplicationIcon(packageName)
            drawable.toBitmap(width = 144, height = 144).asImageBitmap()
        } catch (error: Exception) {
            AppLogger.error("load_game_icon", error, mapOf("packageName" to packageName))
            null
        }
    }
    Surface(
        modifier = Modifier.size(GameSpaceDimensions.gameIcon).clip(RoundedCornerShape(GameSpaceDimensions.iconCornerRadius)),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        if (bitmap == null) {
            Icon(Icons.Filled.SportsEsports, null, modifier = Modifier.padding(GameSpaceDimensions.space16))
        } else {
            Image(BitmapPainter(bitmap), null, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun sessionStatus(state: SessionState) {
    val label = stringResource(
        when (state) {
            SessionState.starting -> R.string.session_starting
            SessionState.running -> R.string.session_running
            SessionState.paused -> R.string.session_paused
            SessionState.stopped -> R.string.session_stopped
            SessionState.failed -> R.string.session_failed
        },
    )
    Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun emptyLibrary(onAdd: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(GameSpaceDimensions.space16),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(GameSpaceDimensions.space8, Alignment.CenterVertically),
    ) {
        Icon(Icons.Filled.SportsEsports, null, Modifier.size(GameSpaceDimensions.emptyIcon), MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.empty_library_title), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.empty_library_description), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Button(onClick = onAdd) {
            Icon(Icons.Filled.Add, null)
            Text(stringResource(R.string.add_game))
        }
    }
}
