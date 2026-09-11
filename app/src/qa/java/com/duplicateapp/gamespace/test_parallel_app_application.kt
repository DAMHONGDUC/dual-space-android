package com.duplicateapp.gamespace

import android.content.Context
import com.duplicateapp.gamespace.core.logging.AppLogger
import com.duplicateapp.gamespace.features.virtualization.data.TheUniverseRuntimeBootstrap

class TestParallelAppApplication : ParallelAppApplication() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        try {
            TheUniverseRuntimeBootstrap.attach(base)
            AppLogger.success("virtual_runtime_attach", emptyMap())
        } catch (error: Exception) {
            AppLogger.error("virtual_runtime_attach", error, emptyMap())
        }
    }

    override fun onCreate() {
        super.onCreate()
        try {
            TheUniverseRuntimeBootstrap.create()
            AppLogger.success("virtual_runtime_create", emptyMap())
        } catch (error: Exception) {
            AppLogger.error("virtual_runtime_create", error, emptyMap())
        }
    }
}
