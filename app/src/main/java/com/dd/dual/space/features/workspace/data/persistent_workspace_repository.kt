package com.dd.dual.space.features.workspace.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.dd.dual.space.core.logging.AppLogger
import com.dd.dual.space.features.workspace.domain.GameSession
import com.dd.dual.space.features.workspace.domain.AccountColor
import com.dd.dual.space.features.workspace.domain.ProfileTarget
import com.dd.dual.space.features.workspace.domain.WorkspaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class PersistentWorkspaceRepository(context: Context) : WorkspaceRepository {
    private val preferences: SharedPreferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
    private val mutableSessions = MutableStateFlow(loadSessions())
    private val mutableSelectedSessionId = MutableStateFlow(loadSelectedSessionId(mutableSessions.value))

    override val sessions: StateFlow<List<GameSession>> = mutableSessions.asStateFlow()
    override val selectedSessionId: StateFlow<String?> = mutableSelectedSessionId.asStateFlow()

    init {
        if (!preferences.contains(sessionsKey) && hasLegacySessions()) {
            update(mutableSessions.value)
            preferences.edit { putString(selectedSessionKey, mutableSelectedSessionId.value) }
            AppLogger.success("migrate_workspace_sessions", mapOf("count" to mutableSessions.value.size, "version" to 6))
        }
    }

    override suspend fun selectSession(sessionId: String) {
        AppLogger.action("select_session", mapOf("sessionId" to sessionId))
        if (mutableSessions.value.none { session -> session.id == sessionId }) {
            AppLogger.error("select_session", IllegalArgumentException(sessionId), mapOf("sessionId" to sessionId))
            return
        }
        mutableSelectedSessionId.value = sessionId
        preferences.edit { putString(selectedSessionKey, sessionId) }
        AppLogger.success("select_session", mapOf("sessionId" to sessionId))
    }

    override suspend fun recordSessionOpened(sessionId: String, openedAtEpochMillis: Long) {
        AppLogger.action("record_session_opened", mapOf("sessionId" to sessionId))
        update(mutableSessions.value.map { session ->
            if (session.id == sessionId) session.copy(lastOpenedAtEpochMillis = openedAtEpochMillis) else session
        })
        AppLogger.success("record_session_opened", mapOf("sessionId" to sessionId))
    }

    override suspend fun updateSessionIdentity(sessionId: String, name: String, accountColor: AccountColor) {
        AppLogger.action("update_session_identity", mapOf("sessionId" to sessionId, "color" to accountColor.name))
        val normalizedName: String = name.trim()
        if (normalizedName.isEmpty()) {
            AppLogger.error("update_session_identity", IllegalArgumentException("empty_name"), mapOf("sessionId" to sessionId))
            return
        }
        update(mutableSessions.value.map { session ->
            if (session.id == sessionId) session.copy(name = normalizedName, accountColor = accountColor) else session
        })
        AppLogger.success("update_session_identity", mapOf("sessionId" to sessionId, "color" to accountColor.name))
    }

    override suspend fun addSession(
        name: String,
        gameName: String,
        packageName: String,
        profileTarget: ProfileTarget,
        virtualUserId: Int,
    ) {
        AppLogger.action("add_session", mapOf("packageName" to packageName, "virtualUserId" to virtualUserId))
        val session = GameSession(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            gameName = gameName,
            accountColor = AccountColor.entries[mutableSessions.value.size % AccountColor.entries.size],
            lastOpenedAtEpochMillis = null,
            packageName = packageName,
            profileTarget = profileTarget,
            virtualUserId = virtualUserId,
        )
        update(mutableSessions.value + session)
        AppLogger.success("add_session", mapOf("sessionId" to session.id))
    }

    override suspend fun deleteSession(sessionId: String) {
        AppLogger.action("delete_session", mapOf("sessionId" to sessionId))
        update(mutableSessions.value.filterNot { session -> session.id == sessionId })
        if (mutableSelectedSessionId.value == sessionId) {
            val replacementId: String? = mutableSessions.value.firstOrNull()?.id
            mutableSelectedSessionId.value = replacementId
            preferences.edit { putString(selectedSessionKey, replacementId) }
        }
        AppLogger.success("delete_session", mapOf("sessionId" to sessionId))
    }

    private fun loadSelectedSessionId(sessions: List<GameSession>): String? {
        val savedSessionId: String? = preferences.getString(selectedSessionKey, null)
            ?: preferences.getString(previousSelectedSessionKey, null)
            ?: preferences.getString(oldestSelectedSessionKey, null)
        return sessions.firstOrNull { session -> session.id == savedSessionId }?.id ?: sessions.firstOrNull()?.id
    }

    private fun loadSessions(): List<GameSession> {
        val encoded: String = preferences.getString(sessionsKey, null)
            ?: preferences.getString(previousSessionsKey, null)
            ?: preferences.getString(oldestSessionsKey, null)
            ?: return emptyList()
        val array: JSONArray = try {
            JSONArray(encoded)
        } catch (error: Exception) {
            AppLogger.error("load_sessions", error, mapOf("length" to encoded.length))
            preserveUnreadableSessions(encoded)
            return emptyList()
        }
        val nextVirtualUserIdByPackage = mutableMapOf<String, Int>()
        val seenIds = mutableSetOf<String>()
        val sessions = mutableListOf<GameSession>()
        for (index in 0 until array.length()) {
            // One damaged record must not discard the accounts that are still readable.
            val session: GameSession? = try {
                val item: JSONObject = array.getJSONObject(index)
                val packageName: String = item.getString("packageName")
                val migratedVirtualUserId: Int = nextVirtualUserIdByPackage.getOrDefault(packageName, 1)
                val virtualUserId: Int = item.optInt("virtualUserId", migratedVirtualUserId)
                nextVirtualUserIdByPackage[packageName] = maxOf(nextVirtualUserIdByPackage.getOrDefault(packageName, 1), virtualUserId + 1)
                GameSession(
                    id = item.getString("id"),
                    name = item.getString("name"),
                    gameName = item.getString("gameName"),
                    accountColor = item.optString("accountColor").takeIf(String::isNotEmpty)
                        ?.let { color -> runCatching { AccountColor.valueOf(color) }.getOrNull() }
                        ?: AccountColor.entries[index % AccountColor.entries.size],
                    lastOpenedAtEpochMillis = item.optLong("lastOpenedAtEpochMillis").takeIf { value -> value > 0L },
                    packageName = packageName,
                    profileTarget = ProfileTarget.valueOf(item.getString("profileTarget")),
                    virtualUserId = virtualUserId,
                ).takeIf { candidate ->
                    candidate.id.isNotBlank() && candidate.packageName.isNotBlank() &&
                        candidate.virtualUserId >= 0 && seenIds.add(candidate.id)
                }
            } catch (error: Exception) {
                AppLogger.error("load_session_record", error, mapOf("index" to index))
                null
            }
            if (session == null) {
                AppLogger.error("load_session_record", IllegalStateException("invalid_record"), mapOf("index" to index))
            } else {
                sessions += session
            }
        }
        if (sessions.size != array.length()) {
            AppLogger.error(
                "load_sessions",
                IllegalStateException("skipped_records"),
                mapOf("loaded" to sessions.size, "stored" to array.length()),
            )
            preserveUnreadableSessions(encoded)
        }
        return sessions
    }

    // Keeps the first damaged payload so later saves cannot destroy the only recoverable copy.
    private fun preserveUnreadableSessions(encoded: String) {
        if (preferences.contains(recoveryBackupKey)) return
        val saved: Boolean = preferences.edit().putString(recoveryBackupKey, encoded).commit()
        AppLogger.success("backup_unreadable_sessions", mapOf("saved" to saved, "length" to encoded.length))
    }

    private fun update(sessions: List<GameSession>) {
        val encoded = JSONArray().apply {
            sessions.forEach { session ->
                put(JSONObject().apply {
                    put("id", session.id)
                    put("name", session.name)
                    put("gameName", session.gameName)
                    put("packageName", session.packageName)
                    put("profileTarget", session.profileTarget.name)
                    put("virtualUserId", session.virtualUserId)
                    put("accountColor", session.accountColor.name)
                    session.lastOpenedAtEpochMillis?.let { value -> put("lastOpenedAtEpochMillis", value) }
                })
            }
        }.toString()
        // Publish only what storage accepted, so the UI never shows accounts that were not saved.
        if (!preferences.edit().putString(sessionsKey, encoded).commit()) {
            AppLogger.error("save_sessions", IllegalStateException("commit_failed"), mapOf("count" to sessions.size))
            return
        }
        mutableSessions.value = sessions
    }

    private fun hasLegacySessions(): Boolean =
        preferences.contains(previousSessionsKey) || preferences.contains(oldestSessionsKey)

    private companion object {
        const val preferencesName = "workspace"
        const val sessionsKey = "sessions_v6"
        const val previousSessionsKey = "sessions_v5"
        const val oldestSessionsKey = "sessions_v4"
        const val selectedSessionKey = "selected_session_v6"
        const val previousSelectedSessionKey = "selected_session_v5"
        const val oldestSelectedSessionKey = "selected_session_v4"
        const val recoveryBackupKey = "sessions_recovery_backup"
    }
}
