package com.dd.dual.space.features.workspace.data

import android.content.Context
import android.content.pm.ShortcutManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dd.dual.space.MainActivity
import com.dd.dual.space.features.workspace.domain.AccountColor
import com.dd.dual.space.features.workspace.domain.GameSession
import com.dd.dual.space.features.workspace.domain.ProfileTarget
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidWorkspaceShortcutPublisherTest {
    @Test
    fun shortcutTargetsTheExpectedSession() {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val manager: ShortcutManager = context.getSystemService(ShortcutManager::class.java)
        val publisher = AndroidWorkspaceShortcutPublisher(context)

        publisher.publish(listOf(session))

        val shortcut = manager.dynamicShortcuts.single()
        assertEquals(session.id, shortcut.intent?.getStringExtra(MainActivity.shortcutSessionIdExtra))
        manager.removeAllDynamicShortcuts()
    }

    @Test
    fun blankSessionNameIsSkippedWithoutCrashing() {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val manager: ShortcutManager = context.getSystemService(ShortcutManager::class.java)
        val publisher = AndroidWorkspaceShortcutPublisher(context)

        publisher.publish(listOf(session.copy(id = "blank-session", name = "  "), session))

        assertEquals(listOf("session_" + session.id), manager.dynamicShortcuts.map { shortcut -> shortcut.id })
        manager.removeAllDynamicShortcuts()
    }

    private val session = GameSession(
        id = "shortcut-session",
        name = "Gaming",
        gameName = "Test Game",
        accountColor = AccountColor.blue,
        lastOpenedAtEpochMillis = null,
        packageName = "com.example.game",
        profileTarget = ProfileTarget.managed,
    )
}
