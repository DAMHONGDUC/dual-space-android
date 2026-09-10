package com.duplicateapp.gamespace.features.workspace.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.duplicateapp.gamespace.core.logging.AppLogger
import com.duplicateapp.gamespace.features.workspace.domain.GameSession
import com.duplicateapp.gamespace.features.workspace.domain.ProfileTarget
import com.duplicateapp.gamespace.features.workspace.domain.SessionState
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

    override suspend fun updateSessionState(sessionId: String, state: SessionState) {
        AppLogger.action("update_session_state", mapOf("sessionId" to sessionId, "state" to state.name))
        update(mutableSessions.value.map { session ->
            if (session.id == sessionId) session.copy(state = state) else session
        })
        AppLogger.success("update_session_state", mapOf("sessionId" to sessionId, "state" to state.name))
    }

    override suspend fun addSession(name: String, gameName: String, packageName: String, profileTarget: ProfileTarget) {
        AppLogger.action("add_session", mapOf("packageName" to packageName, "profile" to profileTarget.name))
        val session = GameSession(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            gameName = gameName,
            state = SessionState.stopped,
            cpuPercent = 0,
            memoryGb = 0f,
            temperatureCelsius = 0,
            framesPerSecond = 0,
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
        return sessions.firstOrNull { session -> session.id == savedSessionId }?.id ?: sessions.firstOrNull()?.id
    }

    private fun loadSessions(): List<GameSession> {
        val encoded: String = preferences.getString(sessionsKey, null) ?: return emptyList()
        return try {
            val array = JSONArray(encoded)
            List(array.length()) { index ->
                val item: JSONObject = array.getJSONObject(index)
                GameSession(
                    id = item.getString("id"),
                    name = item.getString("name"),
                    gameName = item.getString("gameName"),
                    state = SessionState.stopped,
                    cpuPercent = 0,
                    memoryGb = 0f,
                    temperatureCelsius = 0,
                    framesPerSecond = 0,
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
                })
            }
        }.toString()
        preferences.edit { putString(sessionsKey, encoded) }
    }

    private companion object {
        const val preferencesName = "workspace"
        const val sessionsKey = "sessions_v4"
        const val selectedSessionKey = "selected_session_v4"
    }
}
