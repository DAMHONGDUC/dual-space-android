package com.duplicateapp.gamespace.features.virtualization.data

import android.content.Context
import com.duplicateapp.theuniverse.TheUniverseCore
import com.duplicateapp.theuniverse.app.configuration.ClientConfiguration
import java.io.File

object TheUniverseRuntimeBootstrap {
    fun attach(context: Context) {
        val core: TheUniverseCore = TheUniverseCore.get()
        core.closeCodeInit()
        core.doAttachBaseContext(context, ParallelAppClientConfiguration(context.packageName))
    }

    fun create() {
        TheUniverseCore.get().doCreate()
    }

    private class ParallelAppClientConfiguration(
        private val packageName: String,
    ) : ClientConfiguration() {
        override fun getHostPackageName(): String = packageName
        override fun isHideRoot(): Boolean = false
        override fun isEnableDaemonService(): Boolean = false
        override fun isEnableLauncherActivity(): Boolean = false
        override fun isUseVpnNetwork(): Boolean = false
        override fun isDisableFlagSecure(): Boolean = false
        override fun requestInstallPackage(file: File?, userId: Int): Boolean = false
        override fun getLogSenderChatId(): String = ""
    }
}
