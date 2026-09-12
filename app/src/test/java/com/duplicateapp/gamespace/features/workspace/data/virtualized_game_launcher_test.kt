package com.duplicateapp.gamespace.features.workspace.data

import com.duplicateapp.gamespace.features.virtualization.domain.VirtualGameRuntime
import com.duplicateapp.gamespace.features.virtualization.domain.VirtualRuntimeResult
import com.duplicateapp.gamespace.features.workspace.domain.AccountColor
import com.duplicateapp.gamespace.features.workspace.domain.GameLaunchReadiness
import com.duplicateapp.gamespace.features.workspace.domain.GameLaunchResult
import com.duplicateapp.gamespace.features.workspace.domain.GameSession
import com.duplicateapp.gamespace.features.workspace.domain.ProfileTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VirtualizedGameLauncherTest {
    @Test
    fun `first launch installs copy then opens it`() {
        val runtime = FakeVirtualGameRuntime(isInstalled = false)
        val launcher = VirtualizedGameLauncher(runtime)

        val result: GameLaunchResult = launcher.launch(session)

        assertTrue(result is GameLaunchResult.Opened)
        assertEquals(listOf("install:1", "launch:1"), runtime.actions)
    }

    @Test
    fun `installed copy launches without reinstalling`() {
        val runtime = FakeVirtualGameRuntime(isInstalled = true)
        val launcher = VirtualizedGameLauncher(runtime)

        assertEquals(GameLaunchReadiness.Ready, launcher.readiness(session))
        launcher.launch(session)

        assertEquals(listOf("launch:1"), runtime.actions)
    }

    @Test
    fun `second copy uses its own virtual user`() {
        val runtime = FakeVirtualGameRuntime(isInstalled = false)
        val launcher = VirtualizedGameLauncher(runtime)

        launcher.launch(session.copy(virtualUserId = 2))

        assertEquals(listOf("install:2", "launch:2"), runtime.actions)
    }

    @Test
    fun `removing a copy uninstalls only its virtual user`() {
        val runtime = FakeVirtualGameRuntime(isInstalled = true)
        val launcher = VirtualizedGameLauncher(runtime)

        assertTrue(launcher.remove(session.copy(virtualUserId = 2)))

        assertEquals(listOf("uninstall:2"), runtime.actions)
    }

    @Test
    fun `failed uninstall keeps removal unsuccessful`() {
        val runtime = FakeVirtualGameRuntime(
            isInstalled = true,
            uninstallResult = VirtualRuntimeResult.Failure("still installed"),
        )
        val launcher = VirtualizedGameLauncher(runtime)

        assertTrue(!launcher.remove(session))
        assertEquals(listOf("uninstall:1"), runtime.actions)
    }

    private class FakeVirtualGameRuntime(
        private val isInstalled: Boolean,
        private val uninstallResult: VirtualRuntimeResult = VirtualRuntimeResult.Success,
    ) : VirtualGameRuntime {
        val actions = mutableListOf<String>()

        override fun installFromDevice(packageName: String, virtualUserId: Int): VirtualRuntimeResult {
            actions += "install:$virtualUserId"
            return VirtualRuntimeResult.Success
        }

        override fun isInstalled(packageName: String, virtualUserId: Int): Boolean = isInstalled

        override fun launch(packageName: String, virtualUserId: Int): VirtualRuntimeResult {
            actions += "launch:$virtualUserId"
            return VirtualRuntimeResult.Success
        }

        override fun uninstall(packageName: String, virtualUserId: Int): VirtualRuntimeResult {
            actions += "uninstall:$virtualUserId"
            return uninstallResult
        }
    }

    private companion object {
        val session = GameSession(
            id = "copy",
            name = "Account 2",
            gameName = "Game",
            accountColor = AccountColor.blue,
            lastOpenedAtEpochMillis = null,
            packageName = "com.example.game",
            profileTarget = ProfileTarget.managed,
        )
    }
}
