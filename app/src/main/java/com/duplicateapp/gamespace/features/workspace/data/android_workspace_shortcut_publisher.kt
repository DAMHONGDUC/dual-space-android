package com.duplicateapp.gamespace.features.workspace.data

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import com.duplicateapp.gamespace.MainActivity
import com.duplicateapp.gamespace.R
import com.duplicateapp.gamespace.core.logging.AppLogger
import com.duplicateapp.gamespace.features.workspace.domain.GameSession
import com.duplicateapp.gamespace.features.workspace.domain.WorkspaceShortcutPublisher

class AndroidWorkspaceShortcutPublisher(private val context: Context) : WorkspaceShortcutPublisher {
    private val shortcutManager: ShortcutManager = context.getSystemService(ShortcutManager::class.java)

    override fun publish(sessions: List<GameSession>) {
        AppLogger.action("publish_workspace_shortcuts", mapOf("count" to sessions.size))
        val shortcuts: List<ShortcutInfo> = sessions.take(maximumDynamicShortcuts).map { session ->
            ShortcutInfo.Builder(context, shortcutIdPrefix + session.id)
                .setShortLabel(session.name)
                .setLongLabel(context.getString(R.string.shortcut_long_label, session.name, session.gameName))
                .setIcon(Icon.createWithResource(context, R.mipmap.ic_launcher))
                .setIntent(
                    Intent(context, MainActivity::class.java)
                        .setAction(Intent.ACTION_VIEW)
                        .putExtra(MainActivity.shortcutSessionIdExtra, session.id),
                )
                .build()
        }
        try {
            shortcutManager.dynamicShortcuts = shortcuts
            AppLogger.success("publish_workspace_shortcuts", mapOf("count" to shortcuts.size))
        } catch (error: Exception) {
            AppLogger.error("publish_workspace_shortcuts", error, mapOf("count" to shortcuts.size))
        }
    }

    private companion object {
        const val maximumDynamicShortcuts: Int = 4
        const val shortcutIdPrefix: String = "session_"
    }
}
