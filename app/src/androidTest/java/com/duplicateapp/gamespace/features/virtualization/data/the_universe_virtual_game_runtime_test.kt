package com.duplicateapp.gamespace.features.virtualization.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.duplicateapp.gamespace.features.virtualization.domain.VirtualRuntimeResult
import com.dd.the.universe.core.env.BEnvironment
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TheUniverseVirtualGameRuntimeTest {
    @Test
    fun virtualUsersKeepIndependentDataAndUninstallInIsolation() {
        val packageName: String = InstrumentationRegistry.getInstrumentation().context.packageName
        val runtime = TheUniverseVirtualGameRuntime()
        val firstUserId = 1
        val secondUserId = 2
        val firstMarker = File(BEnvironment.getDataFilesDir(packageName, firstUserId), markerFileName)
        val secondMarker = File(BEnvironment.getDataFilesDir(packageName, secondUserId), markerFileName)

        runtime.uninstall(packageName, firstUserId)
        runtime.uninstall(packageName, secondUserId)
        try {
            assertEquals(VirtualRuntimeResult.Success, runtime.installFromDevice(packageName, firstUserId))
            assertEquals(VirtualRuntimeResult.Success, runtime.installFromDevice(packageName, secondUserId))

            firstMarker.parentFile?.mkdirs()
            secondMarker.parentFile?.mkdirs()
            firstMarker.writeText(firstMarkerValue)
            secondMarker.writeText(secondMarkerValue)

            assertEquals(firstMarkerValue, firstMarker.readText())
            assertEquals(secondMarkerValue, secondMarker.readText())
            assertTrue(runtime.isInstalled(packageName, firstUserId))
            assertTrue(runtime.isInstalled(packageName, secondUserId))

            assertEquals(VirtualRuntimeResult.Success, runtime.uninstall(packageName, firstUserId))

            assertFalse(firstMarker.exists())
            assertFalse(runtime.isInstalled(packageName, firstUserId))
            assertEquals(secondMarkerValue, secondMarker.readText())
            assertTrue(runtime.isInstalled(packageName, secondUserId))
        } finally {
            runtime.uninstall(packageName, firstUserId)
            runtime.uninstall(packageName, secondUserId)
        }
    }

    private companion object {
        const val markerFileName = "isolation_marker.txt"
        const val firstMarkerValue = "copy-one"
        const val secondMarkerValue = "copy-two"
    }
}
