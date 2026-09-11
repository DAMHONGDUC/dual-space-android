package com.duplicateapp.gamespace.features.workspace.data

import android.content.Context
import com.duplicateapp.gamespace.features.virtualization.data.TheUniverseVirtualGameRuntime
import com.duplicateapp.gamespace.features.workspace.domain.GameLauncher

object GameLauncherProvider {
    @Suppress("UNUSED_PARAMETER")
    fun create(context: Context): GameLauncher = VirtualizedGameLauncher(TheUniverseVirtualGameRuntime())
}
