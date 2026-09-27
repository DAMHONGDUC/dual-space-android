package com.dd.dual.space.features.workspace.data

import com.dd.dual.space.features.virtualization.domain.FailureKind
import com.dd.dual.space.features.virtualization.domain.VirtualGameRuntime
import com.dd.dual.space.features.virtualization.domain.VirtualRuntimeResult
import com.dd.dual.space.features.workspace.domain.AccountColor
import com.dd.dual.space.features.workspace.domain.GameLaunchReadiness
import com.dd.dual.space.features.workspace.domain.GameLaunchResult
import com.dd.dual.space.features.workspace.domain.GameSession
import com.dd.dual.space.features.workspace.domain.LaunchUnavailableReason
import com.dd.dual.space.features.workspace.domain.ProfileTarget
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
    fun `running state is scoped to the virtual user`() {
        val runtime = FakeVirtualGameRuntime(isInstalled = true, runningVirtualUserId = 2)
        val launcher = VirtualizedGameLauncher(runtime)

        assertTrue(!launcher.isRunning(session.copy(virtualUserId = 1)))
        assertTrue(launcher.isRunning(session.copy(virtualUserId = 2)))
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

    @Test
    fun `missing original game is reported as not installed`() {
        val runtime = FakeVirtualGameRuntime(
            isInstalled = false,
            sourceInstalled = false,
            installResult = VirtualRuntimeResult.Failure("gone", FailureKind.sourceMissing),
        )
        val launcher = VirtualizedGameLauncher(runtime)

        assertEquals(GameLaunchReadiness.Unavailable(LaunchUnavailableReason.gameNotInstalled), launcher.readiness(session))
        assertEquals(GameLaunchResult.Unavailable(LaunchUnavailableReason.gameNotInstalled), launcher.launch(session))
    }

    @Test
    fun `install failure is not reported as a missing game or permission`() {
        val runtime = FakeVirtualGameRuntime(
            isInstalled = false,
            installResult = VirtualRuntimeResult.Failure("disk full", FailureKind.installFailed),
        )

        assertEquals(
            GameLaunchResult.Unavailable(LaunchUnavailableReason.installFailed),
            VirtualizedGameLauncher(runtime).launch(session),
        )
    }

    @Test
    fun `launch failures keep their cause`() {
        val timedOut = FakeVirtualGameRuntime(
            isInstalled = true,
            launchResult = VirtualRuntimeResult.Failure("slow", FailureKind.launchTimedOut),
        )
        val engineDown = FakeVirtualGameRuntime(
            isInstalled = true,
            launchResult = VirtualRuntimeResult.Failure("binder", FailureKind.engineUnavailable),
        )

        assertEquals(GameLaunchResult.Unavailable(LaunchUnavailableReason.launchTimedOut), VirtualizedGameLauncher(timedOut).launch(session))
        assertEquals(GameLaunchResult.Unavailable(LaunchUnavailableReason.engineUnavailable), VirtualizedGameLauncher(engineDown).launch(session))
    }

    @Test
    fun `installed copy stays ready even if the original game was removed`() {
        val runtime = FakeVirtualGameRuntime(isInstalled = true, sourceInstalled = false)

        assertEquals(GameLaunchReadiness.Ready, VirtualizedGameLauncher(runtime).readiness(session))
    }

    private class FakeVirtualGameRuntime(
        private val isInstalled: Boolean,
        private val uninstallResult: VirtualRuntimeResult = VirtualRuntimeResult.Success,
        private val runningVirtualUserId: Int? = null,
        private val sourceInstalled: Boolean = true,
        private val installResult: VirtualRuntimeResult = VirtualRuntimeResult.Success,
        private val launchResult: VirtualRuntimeResult = VirtualRuntimeResult.Success,
    ) : VirtualGameRuntime {
        val actions = mutableListOf<String>()

        override fun installFromDevice(packageName: String, virtualUserId: Int): VirtualRuntimeResult {
            actions += "install:$virtualUserId"
            return installResult
        }

        override fun isInstalled(packageName: String, virtualUserId: Int): Boolean = isInstalled

        override fun launch(packageName: String, virtualUserId: Int): VirtualRuntimeResult {
            actions += "launch:$virtualUserId"
            return launchResult
        }

        override fun isSourceInstalled(packageName: String): Boolean = sourceInstalled

        override fun isRunning(packageName: String, virtualUserId: Int): Boolean =
            virtualUserId == runningVirtualUserId

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
