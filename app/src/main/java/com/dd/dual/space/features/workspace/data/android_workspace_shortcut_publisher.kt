package com.dd.dual.space.features.workspace.data

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import com.dd.dual.space.MainActivity
import com.dd.dual.space.R
import com.dd.dual.space.core.logging.AppLogger
import com.dd.dual.space.features.workspace.domain.GameSession
import com.dd.dual.space.features.workspace.domain.WorkspaceShortcutPublisher

class AndroidWorkspaceShortcutPublisher(private val context: Context) : WorkspaceShortcutPublisher {
    private val shortcutManager: ShortcutManager = context.getSystemService(ShortcutManager::class.java)

    override fun publish(sessions: List<GameSession>) {
        AppLogger.action("publish_workspace_shortcuts", mapOf("count" to sessions.size))
        try {
            val limit: Int = shortcutManager.maxShortcutCountPerActivity.coerceAtMost(maximumDynamicShortcuts)
            val shortcuts: List<ShortcutInfo> = sessions
                .filter { session -> session.id.isNotBlank() && session.name.isNotBlank() }
                .take(limit)
                .map(::shortcutFor)
            shortcutManager.dynamicShortcuts = shortcuts
            disableRemovedPinnedShortcuts(sessions.map { session -> shortcutIdPrefix + session.id }.toSet())
            AppLogger.success("publish_workspace_shortcuts", mapOf("count" to shortcuts.size))
        } catch (error: Exception) {
            AppLogger.error("publish_workspace_shortcuts", error, mapOf("count" to sessions.size))
        }
    }

    private fun shortcutFor(session: GameSession): ShortcutInfo =
        ShortcutInfo.Builder(context, shortcutIdPrefix + session.id)
            .setShortLabel(session.name.trim())
            .setLongLabel(context.getString(R.string.shortcut_long_label, session.name.trim(), session.gameName))
            .setIcon(Icon.createWithResource(context, R.mipmap.ic_launcher))
            .setIntent(
                Intent(context, MainActivity::class.java)
                    .setAction(Intent.ACTION_VIEW)
                    .putExtra(MainActivity.shortcutSessionIdExtra, session.id),
            )
            .build()

    // Pinned shortcuts survive dynamic list updates, so deleted accounts must be disabled explicitly.
    private fun disableRemovedPinnedShortcuts(liveIds: Set<String>) {
        val staleIds: List<String> = shortcutManager.pinnedShortcuts
            .map(ShortcutInfo::getId)
            .filter { id -> id.startsWith(shortcutIdPrefix) && id !in liveIds }
        if (staleIds.isEmpty()) return
        shortcutManager.disableShortcuts(staleIds, context.getString(R.string.shortcut_removed_account))
        AppLogger.success("disable_removed_shortcuts", mapOf("count" to staleIds.size))
    }

    private companion object {
        const val maximumDynamicShortcuts: Int = 4
        const val shortcutIdPrefix: String = "session_"
    }
}
