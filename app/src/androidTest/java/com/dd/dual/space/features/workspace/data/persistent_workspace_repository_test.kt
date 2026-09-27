package com.dd.dual.space.features.workspace.data

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PersistentWorkspaceRepositoryTest {
    @Test
    fun versionFourSessionsMigrateWithoutLosingAccount() {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
        preferences.edit().clear().commit()
        val encoded: String = JSONArray().put(
            JSONObject()
                .put("id", sessionId)
                .put("name", "Work")
                .put("gameName", "Test Game")
                .put("packageName", "com.example.game")
                .put("profileTarget", "managed"),
        ).toString()
        preferences.edit().putString(legacySessionsKey, encoded).putString(legacySelectedKey, sessionId).commit()

        val repository = PersistentWorkspaceRepository(context)

        assertEquals(sessionId, repository.selectedSessionId.value)
        assertEquals("Work", repository.sessions.value.single().name)
        assertEquals(1, repository.sessions.value.single().virtualUserId)
        assertTrue(preferences.contains(currentSessionsKey))
        preferences.edit().clear().commit()
    }

    @Test
    fun damagedRecordKeepsValidAccountsAndBacksUpOriginal() {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
        preferences.edit().clear().commit()
        val encoded: String = JSONArray()
            .put(session(sessionId, virtualUserId = 1))
            .put(session("unknown-target", virtualUserId = 2).put("profileTarget", "removed_target"))
            .put(session(sessionId, virtualUserId = 3))
            .put(session("negative-user", virtualUserId = -1))
            .put(JSONObject().put("id", "missing-fields"))
            .toString()
        preferences.edit().putString(currentSessionsKey, encoded).commit()

        val repository = PersistentWorkspaceRepository(context)

        assertEquals(listOf(sessionId), repository.sessions.value.map { session -> session.id })
        assertEquals(encoded, preferences.getString(recoveryBackupKey, null))
        preferences.edit().clear().commit()
    }

    @Test
    fun unreadablePayloadIsBackedUpBeforeAnySave() {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
        preferences.edit().clear().commit()
        preferences.edit().putString(currentSessionsKey, "[{truncated").commit()

        val repository = PersistentWorkspaceRepository(context)

        assertTrue(repository.sessions.value.isEmpty())
        assertEquals("[{truncated", preferences.getString(recoveryBackupKey, null))
        preferences.edit().clear().commit()
    }

    private fun session(id: String, virtualUserId: Int): JSONObject = JSONObject()
        .put("id", id)
        .put("name", "Work")
        .put("gameName", "Test Game")
        .put("packageName", "com.example.game")
        .put("profileTarget", "managed")
        .put("virtualUserId", virtualUserId)

    private companion object {
        const val recoveryBackupKey: String = "sessions_recovery_backup"
        const val preferencesName: String = "workspace"
        const val legacySessionsKey: String = "sessions_v4"
        const val legacySelectedKey: String = "selected_session_v4"
        const val currentSessionsKey: String = "sessions_v6"
        const val sessionId: String = "legacy-session"
    }
}
