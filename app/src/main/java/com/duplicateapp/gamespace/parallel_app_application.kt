package com.duplicateapp.gamespace

import android.app.Application
import android.content.Context
import android.os.SystemClock
import com.duplicateapp.gamespace.core.logging.AppLogger
import com.duplicateapp.gamespace.features.virtualization.data.TheUniverseRuntimeBootstrap

class ParallelAppApplication : Application() {
    override fun attachBaseContext(base: Context) {
        val startedAtNanos: Long = SystemClock.elapsedRealtimeNanos()
        super.attachBaseContext(base)
        try {
            TheUniverseRuntimeBootstrap.attach(base)
            AppLogger.success("virtual_runtime_attach", emptyMap())
        } catch (error: Exception) {
            AppLogger.error("virtual_runtime_attach", error, emptyMap())
        } finally {
            AppLogger.success("startup_runtime_attach_timing", timingMetadata(startedAtNanos))
        }
    }

    override fun onCreate() {
        val startedAtNanos: Long = SystemClock.elapsedRealtimeNanos()
        super.onCreate()
        try {
            TheUniverseRuntimeBootstrap.create()
            AppLogger.success("virtual_runtime_create", emptyMap())
        } catch (error: Exception) {
            AppLogger.error("virtual_runtime_create", error, emptyMap())
        } finally {
            AppLogger.success("startup_runtime_create_timing", timingMetadata(startedAtNanos))
        }
    }

    private fun timingMetadata(startedAtNanos: Long): Map<String, Any> = mapOf(
        "durationMs" to (SystemClock.elapsedRealtimeNanos() - startedAtNanos) / nanosPerMillisecond,
    )

    private companion object {
        const val nanosPerMillisecond: Long = 1_000_000L
    }
}
