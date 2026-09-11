package com.duplicateapp.gamespace.features.workspace.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
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
import com.duplicateapp.gamespace.core.theme.ParallelAppDimensions
import com.duplicateapp.gamespace.features.workspace.domain.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class GameRowModel(
    val packageName: String,
    val gameName: String,
    val sessions: List<GameSession>,
)

@Composable
fun gameLibrary(
    sessions: List<GameSession>,
    profileStatus: ProfileProvisioningStatus,
    contentPadding: PaddingValues,
    onLaunch: (String) -> Unit,
    onAdd: () -> Unit,
    onDeleteGame: (String) -> Unit,
    onCreateProfile: () -> Unit,
    onOpenAndroidSettings: () -> Unit,
    onSettings: () -> Unit,
) {
    val gameRows: List<GameRowModel> = remember(sessions) {
        sessions.groupBy(GameSession::packageName).map { (packageName, gameSessions) ->
            GameRowModel(packageName, gameSessions.first().gameName, gameSessions)
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
        Column(modifier = Modifier.fillMaxSize()) {
            libraryTopBar(sessions.size, onSettings)
            if (profileStatus != ProfileProvisioningStatus.alreadyCreated) {
                profileHealthCard(profileStatus, onCreateProfile, onOpenAndroidSettings)
            }
            if (gameRows.isEmpty()) {
                emptyLibrary(onAdd, Modifier.weight(1f))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = ParallelAppDimensions.space16,
                        top = ParallelAppDimensions.space12,
                        end = ParallelAppDimensions.space16,
                        bottom = ParallelAppDimensions.fabClearance,
                    ),
                    verticalArrangement = Arrangement.spacedBy(ParallelAppDimensions.space12),
                ) {
                    items(gameRows, key = GameRowModel::packageName) { game -> gameRow(game, onLaunch, onDeleteGame) }
                }
            }
        }
        FloatingActionButton(
            onClick = onAdd,
            modifier = Modifier.align(Alignment.BottomEnd).padding(ParallelAppDimensions.space16),
        ) { Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_game)) }
    }
}

@Composable
private fun profileHealthCard(
    profileStatus: ProfileProvisioningStatus,
    onCreateProfile: () -> Unit,
    onOpenAndroidSettings: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = ParallelAppDimensions.space16),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(ParallelAppDimensions.space12),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ParallelAppDimensions.space12),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.second_copy_check_title), fontWeight = FontWeight.SemiBold)
                Text(
                    stringResource(
                        if (profileStatus == ProfileProvisioningStatus.available) {
                            R.string.second_copy_setup_description
                        } else {
                            R.string.second_copy_unsupported
                        },
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            TextButton(
                onClick = if (profileStatus == ProfileProvisioningStatus.available) onCreateProfile else onOpenAndroidSettings,
            ) {
                Text(
                    stringResource(
                        if (profileStatus == ProfileProvisioningStatus.available) {
                            R.string.enable_second_copy
                        } else {
                            R.string.android_settings
                        },
                    ),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun libraryTopBar(sessionCount: Int, onSettings: () -> Unit) {
    TopAppBar(
        title = {
            Column {
                Text(stringResource(R.string.app_name), fontWeight = FontWeight.SemiBold)
                Text(
                    stringResource(R.string.copy_count, sessionCount),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        actions = {
            IconButton(onClick = onSettings) {
                Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.settings))
            }
        },
    )
}

@Composable
private fun gameRow(game: GameRowModel, onLaunch: (String) -> Unit, onDeleteGame: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(ParallelAppDimensions.space16),
            verticalArrangement = Arrangement.spacedBy(ParallelAppDimensions.space12),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    game.gameName,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                IconButton(onClick = { onDeleteGame(game.packageName) }) {
                    Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.delete_game))
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ParallelAppDimensions.space12),
            ) {
                gameIcon(game.packageName)
                Spacer(modifier = Modifier.weight(1f))
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(ParallelAppDimensions.space8),
                ) {
                    game.sessions.forEach { session -> accountButton(session, onLaunch) }
                }
            }
        }
    }
}

@Composable
private fun accountButton(session: GameSession, onLaunch: (String) -> Unit) {
    Surface(
        onClick = { onLaunch(session.id) },
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Row(
            modifier = Modifier.defaultMinSize(minHeight = ParallelAppDimensions.touchTarget)
                .padding(horizontal = ParallelAppDimensions.space12, vertical = ParallelAppDimensions.space8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(ParallelAppDimensions.iconSmall))
            Column(modifier = Modifier.padding(start = ParallelAppDimensions.space6)) {
                Text(
                    session.name,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                )
                Text(
                    stringResource(if (session.profileTarget == ProfileTarget.personal) R.string.original_copy else R.string.copy_one),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun gameIcon(packageName: String) {
    val context = LocalContext.current
    val bitmapState = produceState<ImageBitmap?>(initialValue = null, packageName) {
        value = withContext(Dispatchers.IO) {
            try {
                context.packageManager.getApplicationIcon(packageName)
                    .toBitmap(width = gameIconBitmapSize, height = gameIconBitmapSize)
                    .asImageBitmap()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                AppLogger.error("load_game_icon", error, mapOf("packageName" to packageName))
                null
            }
        }
    }
    val bitmap: ImageBitmap? = bitmapState.value
    Surface(
        modifier = Modifier.size(ParallelAppDimensions.gameIcon).clip(RoundedCornerShape(ParallelAppDimensions.iconCornerRadius)),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        if (bitmap == null) {
            Icon(Icons.Filled.SportsEsports, null, modifier = Modifier.padding(ParallelAppDimensions.space16))
        } else {
            Image(BitmapPainter(bitmap), null, modifier = Modifier.fillMaxSize())
        }
    }
}

private const val gameIconBitmapSize: Int = 144

@Composable
private fun sessionStateLabel(state: SessionState): String = stringResource(
    when (state) {
        SessionState.starting -> R.string.session_starting
        SessionState.running -> R.string.session_running
        SessionState.paused -> R.string.session_paused
        SessionState.stopped -> R.string.session_stopped
        SessionState.failed -> R.string.session_failed
    },
)

@Composable
private fun emptyLibrary(onAdd: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(ParallelAppDimensions.space16),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ParallelAppDimensions.space8, Alignment.CenterVertically),
    ) {
        Icon(Icons.Filled.SportsEsports, null, Modifier.size(ParallelAppDimensions.emptyIcon), MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.empty_library_title), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.empty_library_description), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Button(onClick = onAdd) {
            Icon(Icons.Filled.Add, null)
            Text(stringResource(R.string.add_game))
        }
    }
}
