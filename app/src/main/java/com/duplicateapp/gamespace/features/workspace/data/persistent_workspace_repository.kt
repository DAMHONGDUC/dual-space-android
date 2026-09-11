package com.duplicateapp.gamespace.features.workspace.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.duplicateapp.gamespace.core.logging.AppLogger
import com.duplicateapp.gamespace.features.workspace.domain.GameSession
import com.duplicateapp.gamespace.features.workspace.domain.AccountColor
import com.duplicateapp.gamespace.features.workspace.domain.ProfileTarget
import com.duplicateapp.gamespace.features.workspace.domain.WorkspaceRepository
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
        if (!preferences.contains(sessionsKey) && preferences.contains(legacySessionsKey)) {
            update(mutableSessions.value)
            preferences.edit { putString(selectedSessionKey, mutableSelectedSessionId.value) }
            AppLogger.success("migrate_workspace_sessions", mapOf("count" to mutableSessions.value.size, "version" to 5))
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

    override suspend fun addSession(name: String, gameName: String, packageName: String, profileTarget: ProfileTarget) {
        AppLogger.action("add_session", mapOf("packageName" to packageName, "profile" to profileTarget.name))
        val session = GameSession(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            gameName = gameName,
            accountColor = AccountColor.entries[mutableSessions.value.size % AccountColor.entries.size],
            lastOpenedAtEpochMillis = null,
            packageName = packageName,
            profileTarget = profileTarget,
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
            ?: preferences.getString(legacySelectedSessionKey, null)
        return sessions.firstOrNull { session -> session.id == savedSessionId }?.id ?: sessions.firstOrNull()?.id
    }

    private fun loadSessions(): List<GameSession> {
        val encoded: String = preferences.getString(sessionsKey, null)
            ?: preferences.getString(legacySessionsKey, null)
            ?: return emptyList()
        return try {
            val array = JSONArray(encoded)
            List(array.length()) { index ->
                val item: JSONObject = array.getJSONObject(index)
                GameSession(
                    id = item.getString("id"),
                    name = item.getString("name"),
                    gameName = item.getString("gameName"),
                    accountColor = item.optString("accountColor").takeIf(String::isNotEmpty)
                        ?.let { color -> runCatching { AccountColor.valueOf(color) }.getOrNull() }
                        ?: AccountColor.entries[index % AccountColor.entries.size],
                    lastOpenedAtEpochMillis = item.optLong("lastOpenedAtEpochMillis").takeIf { value -> value > 0L },
                    packageName = item.getString("packageName"),
                    profileTarget = ProfileTarget.valueOf(item.getString("profileTarget")),
                )
            }
        } catch (error: Exception) {
            AppLogger.error("load_sessions", error, mapOf("length" to encoded.length))
            emptyList()
        }
    }

    private fun update(sessions: List<GameSession>) {
        mutableSessions.value = sessions
        val encoded = JSONArray().apply {
            mutableSessions.value.forEach { session ->
                put(JSONObject().apply {
                    put("id", session.id)
                    put("name", session.name)
                    put("gameName", session.gameName)
                    put("packageName", session.packageName)
                    put("profileTarget", session.profileTarget.name)
                    put("accountColor", session.accountColor.name)
                    session.lastOpenedAtEpochMillis?.let { value -> put("lastOpenedAtEpochMillis", value) }
                })
            }
        }.toString()
        preferences.edit { putString(sessionsKey, encoded) }
    }

    private companion object {
        const val preferencesName = "workspace"
        const val sessionsKey = "sessions_v5"
        const val legacySessionsKey = "sessions_v4"
        const val selectedSessionKey = "selected_session_v5"
        const val legacySelectedSessionKey = "selected_session_v4"
    }
}
