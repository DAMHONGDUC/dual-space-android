package com.duplicateapp.gamespace

import android.app.Application
import android.content.Context
import com.duplicateapp.gamespace.core.logging.app_logger
import com.duplicateapp.gamespace.features.virtualization.data.the_universe_runtime_bootstrap

class game_space_application : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        try {
            the_universe_runtime_bootstrap.attach(base)
            app_logger.success("virtual_runtime_attach", emptyMap())
        } catch (error: Exception) {
            app_logger.error("virtual_runtime_attach", error, emptyMap())
        }
    }

    override fun onCreate() {
        super.onCreate()
        try {
            the_universe_runtime_bootstrap.create()
            app_logger.success("virtual_runtime_create", emptyMap())
        } catch (error: Exception) {
            app_logger.error("virtual_runtime_create", error, emptyMap())
        }
    }
}
