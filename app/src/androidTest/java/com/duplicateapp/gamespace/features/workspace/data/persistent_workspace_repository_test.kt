package com.duplicateapp.gamespace.features.workspace.data

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
        assertTrue(preferences.contains(currentSessionsKey))
        preferences.edit().clear().commit()
    }

    private companion object {
        const val preferencesName: String = "workspace"
        const val legacySessionsKey: String = "sessions_v4"
        const val legacySelectedKey: String = "selected_session_v4"
        const val currentSessionsKey: String = "sessions_v5"
        const val sessionId: String = "legacy-session"
    }
}
