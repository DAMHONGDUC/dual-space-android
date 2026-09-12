package com.duplicateapp.gamespace.features.workspace.data

import android.content.Context
import com.duplicateapp.gamespace.features.workspace.domain.GameLauncher

object GameLauncherProvider {
    fun create(context: Context, profileResolver: AndroidManagedProfileResolver): GameLauncher =
        AndroidProfileGameLauncher(context, profileResolver)
}
