package com.dd.dual.space.features.virtualization.data

import android.content.pm.PackageManager
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dd.dual.space.features.virtualization.domain.VirtualRuntimeResult
import com.dd.the.universe.core.env.BEnvironment
import java.io.File
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Requires the guest fixture on the device; `scripts/run_guest_e2e.sh` installs it before running. */
@RunWith(AndroidJUnit4::class)
class GuestFixtureLaunchTest {
    private val runtime = TheUniverseVirtualGameRuntime()

    @Before
    fun setUp() {
        val packageManager = InstrumentationRegistry.getInstrumentation().targetContext.packageManager
        val installed: Boolean = try {
            packageManager.getPackageInfo(fixturePackage, 0)
            true
        } catch (error: PackageManager.NameNotFoundException) {
            false
        }
        assumeTrue("guest fixture is not installed; run scripts/run_guest_e2e.sh", installed)
        cleanUp()
    }

    @After
    fun tearDown() = cleanUp()

    @Test
    fun guestLaunchesInsideEachVirtualUserWithIsolatedStorage() {
        assertEquals(VirtualRuntimeResult.Success, runtime.installFromDevice(fixturePackage, firstUserId))
        assertEquals(VirtualRuntimeResult.Success, runtime.installFromDevice(fixturePackage, secondUserId))

        assertEquals(VirtualRuntimeResult.Success, runtime.launch(fixturePackage, firstUserId))
        val firstReport: Map<String, String> = awaitMarker(firstUserId)
        assertEquals(VirtualRuntimeResult.Success, runtime.launch(fixturePackage, secondUserId))
        val secondReport: Map<String, String> = awaitMarker(secondUserId)

        // Each copy starts from empty preferences, so both count their own first launch.
        assertEquals("1", firstReport["launches"])
        assertEquals("1", secondReport["launches"])
        assertNotEquals(firstReport["filesDir"], secondReport["filesDir"])
        assertNotEquals(firstReport["pid"], secondReport["pid"])
    }

    private fun awaitMarker(userId: Int): Map<String, String> {
        val marker = File(BEnvironment.getDataFilesDir(fixturePackage, userId), markerFileName)
        val deadline: Long = SystemClock.elapsedRealtime() + markerTimeoutMillis
        while (SystemClock.elapsedRealtime() < deadline) {
            if (marker.isFile && marker.length() > 0) {
                return marker.readLines().filter { line -> '=' in line }.associate { line ->
                    line.substringBefore('=') to line.substringAfter('=')
                }
            }
            SystemClock.sleep(pollMillis)
        }
        throw AssertionError("guest did not write its marker for user $userId")
    }

    private fun cleanUp() {
        runtime.uninstall(fixturePackage, firstUserId)
        runtime.uninstall(fixturePackage, secondUserId)
    }

    private companion object {
        const val fixturePackage = "com.dd.dual.space.guestfixture"
        const val markerFileName = "guest_marker.txt"
        const val firstUserId = 1
        const val secondUserId = 2
        const val markerTimeoutMillis = 15_000L
        const val pollMillis = 200L
    }
}
