package com.duplicateapp.gamespace.features.workspace.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Color
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
import com.duplicateapp.gamespace.core.theme.ParallelAppColors
import com.duplicateapp.gamespace.features.workspace.domain.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.text.format.DateUtils

private data class GameRowModel(
    val packageName: String,
    val gameName: String,
    val sessions: List<GameSession>,
)

private enum class AccountSortOrder { recent, alphabetical }

@Composable
fun gameLibrary(
    sessions: List<GameSession>,
    profileStatus: ProfileProvisioningStatus,
    readinessBySessionId: Map<String, GameLaunchReadiness> = emptyMap(),
    runningSessionIds: Set<String> = emptySet(),
    contentPadding: PaddingValues,
    onLaunch: (String) -> Unit,
    onAdd: () -> Unit,
    onDeleteGame: (String) -> Unit,
    onEditSession: (String) -> Unit = {},
    onCreateProfile: () -> Unit,
    onOpenAndroidSettings: () -> Unit,
    onHelp: () -> Unit,
    onSettings: () -> Unit,
) {
    var sortOrder by remember { mutableStateOf(AccountSortOrder.recent) }
    val gameRows: List<GameRowModel> = remember(sessions, sortOrder) {
        sessions.groupBy(GameSession::packageName).map { (packageName, gameSessions) ->
            val orderedSessions = when (sortOrder) {
                AccountSortOrder.recent -> gameSessions.sortedWith(
                    compareByDescending<GameSession> { it.lastOpenedAtEpochMillis ?: Long.MIN_VALUE }.thenBy { it.name.lowercase() },
                )
                AccountSortOrder.alphabetical -> gameSessions.sortedBy { it.name.lowercase() }
            }
            GameRowModel(packageName, gameSessions.first().gameName, orderedSessions)
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
        Column(modifier = Modifier.fillMaxSize()) {
            libraryTopBar(sessions.size, onHelp, onSettings)
            if (gameRows.isNotEmpty()) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.padding(horizontal = ParallelAppDimensions.space16),
                ) {
                    AccountSortOrder.entries.forEachIndexed { index, order ->
                        SegmentedButton(
                            selected = sortOrder == order,
                            onClick = { sortOrder = order },
                            shape = SegmentedButtonDefaults.itemShape(index, AccountSortOrder.entries.size),
                            label = {
                                Text(stringResource(if (order == AccountSortOrder.recent) R.string.sort_recent else R.string.sort_name))
                            },
                        )
                    }
                }
            }
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
                    items(gameRows, key = GameRowModel::packageName) { game ->
                        gameRow(game, readinessBySessionId, runningSessionIds, onLaunch, onEditSession, onDeleteGame)
                    }
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
private fun libraryTopBar(sessionCount: Int, onHelp: () -> Unit, onSettings: () -> Unit) {
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
            IconButton(onClick = onHelp) {
                Icon(Icons.AutoMirrored.Outlined.HelpOutline, contentDescription = stringResource(R.string.about_and_help))
            }
            IconButton(onClick = onSettings) {
                Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.settings))
            }
        },
    )
}

@Composable
private fun gameRow(
    game: GameRowModel,
    readinessBySessionId: Map<String, GameLaunchReadiness>,
    runningSessionIds: Set<String>,
    onLaunch: (String) -> Unit,
    onEditSession: (String) -> Unit,
    onDeleteGame: (String) -> Unit,
) {
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
                    game.sessions.forEach { session ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            accountButton(
                                session = session,
                                readiness = readinessBySessionId[session.id],
                                isRunning = session.id in runningSessionIds,
                                onLaunch = onLaunch,
                            )
                            IconButton(onClick = { onEditSession(session.id) }) {
                                Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.edit_account))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun accountButton(
    session: GameSession,
    readiness: GameLaunchReadiness?,
    isRunning: Boolean,
    onLaunch: (String) -> Unit,
) {
    val isReady: Boolean = readiness == GameLaunchReadiness.Ready
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
            Surface(
                modifier = Modifier.size(ParallelAppDimensions.iconSmall),
                shape = CircleShape,
                color = accountColor(session.accountColor),
                contentColor = Color.White,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(session.name.take(1).uppercase(), style = MaterialTheme.typography.labelSmall)
                }
            }
            Column(modifier = Modifier.padding(start = ParallelAppDimensions.space6)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ParallelAppDimensions.space6),
                ) {
                    Text(
                        session.name,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                    )
                    copyTag(session.virtualUserId, session.accountColor)
                }
                Text(
                    if (isRunning) {
                        stringResource(R.string.session_running)
                    } else if (isReady) {
                        session.lastOpenedAtEpochMillis?.let { value ->
                            stringResource(R.string.last_opened, DateUtils.getRelativeTimeSpanString(value))
                        } ?: stringResource(R.string.ready_to_open)
                    } else {
                        stringResource(R.string.needs_attention)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                )
            }
            Icon(
                Icons.Filled.PlayArrow,
                contentDescription = null,
                modifier = Modifier.padding(start = ParallelAppDimensions.space8).size(ParallelAppDimensions.iconSmall),
            )
        }
    }
}

@Composable
private fun copyTag(virtualUserId: Int, copyAccountColor: AccountColor) {
    val backgroundColor: Color = accountColor(copyAccountColor)
    val textColor: Color = Color.White

    Surface(
        shape = RoundedCornerShape(ParallelAppDimensions.space12),
        color = backgroundColor,
        contentColor = textColor,
        border = BorderStroke(
            width = ParallelAppDimensions.borderThin,
            color = textColor.copy(alpha = copyTagBorderAlpha),
        ),
        tonalElevation = ParallelAppDimensions.space2,
    ) {
        Text(
            text = stringResource(R.string.copy_number, virtualUserId),
            modifier = Modifier.padding(
                horizontal = ParallelAppDimensions.space8,
                vertical = ParallelAppDimensions.space4,
            ),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )
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
private const val copyTagBorderAlpha: Float = 0.35f

@Composable
private fun accountColor(accountColor: AccountColor): Color = when (accountColor) {
    AccountColor.blue -> ParallelAppColors.accountBlue
    AccountColor.green -> ParallelAppColors.accountGreen
    AccountColor.orange -> ParallelAppColors.accountOrange
    AccountColor.purple -> ParallelAppColors.accountPurple
}

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
