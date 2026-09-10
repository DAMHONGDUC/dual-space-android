package com.duplicateapp.gamespace.features.workspace.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.duplicateapp.gamespace.core.logging.app_logger
import com.duplicateapp.gamespace.features.workspace.domain.game_session
import com.duplicateapp.gamespace.features.workspace.domain.profile_target
import com.duplicateapp.gamespace.features.workspace.domain.session_state
import com.duplicateapp.gamespace.features.workspace.domain.workspace_repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class persistent_workspace_repository(context: Context) : workspace_repository {
    private val preferences: SharedPreferences = context.getSharedPreferences(preferences_name, Context.MODE_PRIVATE)
    private val mutable_sessions = MutableStateFlow(load_sessions())
    private val mutable_selected_session_id = MutableStateFlow(load_selected_session_id(mutable_sessions.value))

    override val sessions: StateFlow<List<game_session>> = mutable_sessions.asStateFlow()
    override val selected_session_id: StateFlow<String?> = mutable_selected_session_id.asStateFlow()

    override suspend fun select_session(session_id: String) {
        app_logger.action("select_session", mapOf("sessionId" to session_id))
        if (mutable_sessions.value.none { session -> session.id == session_id }) {
            app_logger.error("select_session", IllegalArgumentException(session_id), mapOf("sessionId" to session_id))
            return
        }
        mutable_selected_session_id.value = session_id
        preferences.edit { putString(selected_session_key, session_id) }
        app_logger.success("select_session", mapOf("sessionId" to session_id))
    }

    override suspend fun update_session_state(session_id: String, state: session_state) {
        app_logger.action("update_session_state", mapOf("sessionId" to session_id, "state" to state.name))
        update(mutable_sessions.value.map { session ->
            if (session.id == session_id) session.copy(state = state) else session
        })
        app_logger.success("update_session_state", mapOf("sessionId" to session_id, "state" to state.name))
    }

    override suspend fun add_session(name: String, game_name: String, package_name: String, profile_target: profile_target) {
        app_logger.action("add_session", mapOf("packageName" to package_name, "profile" to profile_target.name))
        val session = game_session(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            game_name = game_name,
            state = session_state.stopped,
            cpu_percent = 0,
            memory_gb = 0f,
            temperature_celsius = 0,
            frames_per_second = 0,
            package_name = package_name,
            profile_target = profile_target,
        )
        update(mutable_sessions.value + session)
        app_logger.success("add_session", mapOf("sessionId" to session.id))
    }

    override suspend fun delete_session(session_id: String) {
        app_logger.action("delete_session", mapOf("sessionId" to session_id))
        update(mutable_sessions.value.filterNot { session -> session.id == session_id })
        if (mutable_selected_session_id.value == session_id) {
            val replacement_id: String? = mutable_sessions.value.firstOrNull()?.id
            mutable_selected_session_id.value = replacement_id
            preferences.edit { putString(selected_session_key, replacement_id) }
        }
        app_logger.success("delete_session", mapOf("sessionId" to session_id))
    }

    private fun load_selected_session_id(sessions: List<game_session>): String? {
        val saved_session_id: String? = preferences.getString(selected_session_key, null)
        return sessions.firstOrNull { session -> session.id == saved_session_id }?.id ?: sessions.firstOrNull()?.id
    }

    private fun load_sessions(): List<game_session> {
        val encoded: String = preferences.getString(sessions_key, null) ?: return emptyList()
        return try {
            val array = JSONArray(encoded)
            List(array.length()) { index ->
                val item: JSONObject = array.getJSONObject(index)
                game_session(
                    id = item.getString("id"),
                    name = item.getString("name"),
                    game_name = item.getString("gameName"),
                    state = session_state.stopped,
                    cpu_percent = 0,
                    memory_gb = 0f,
                    temperature_celsius = 0,
                    frames_per_second = 0,
                    package_name = item.getString("packageName"),
                    profile_target = profile_target.valueOf(item.getString("profileTarget")),
                )
            }
        } catch (error: Exception) {
            app_logger.error("load_sessions", error, mapOf("length" to encoded.length))
            emptyList()
        }
    }

    private fun update(sessions: List<game_session>) {
        mutable_sessions.value = sessions
        val encoded = JSONArray().apply {
            mutable_sessions.value.forEach { session ->
                put(JSONObject().apply {
                    put("id", session.id)
                    put("name", session.name)
                    put("gameName", session.game_name)
                    put("packageName", session.package_name)
                    put("profileTarget", session.profile_target.name)
                })
            }
        }.toString()
        preferences.edit { putString(sessions_key, encoded) }
    }

    private companion object {
        const val preferences_name = "workspace"
        const val sessions_key = "sessions_v4"
        const val selected_session_key = "selected_session_v4"
    }
}
