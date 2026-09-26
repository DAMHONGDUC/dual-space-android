package com.dd.dual.space.features.workspace.data

import android.content.Context
import com.dd.dual.space.features.virtualization.data.TheUniverseVirtualGameRuntime
import com.dd.dual.space.features.workspace.domain.GameLauncher

object GameLauncherProvider {
    @Suppress("UNUSED_PARAMETER")
    fun create(context: Context): GameLauncher = VirtualizedGameLauncher(TheUniverseVirtualGameRuntime())
}
