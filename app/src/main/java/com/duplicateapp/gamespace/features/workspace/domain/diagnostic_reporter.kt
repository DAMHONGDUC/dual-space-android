package com.duplicateapp.gamespace.features.workspace.domain

interface DiagnosticReporter {
    fun share(profileTarget: ProfileTarget, readiness: GameLaunchReadiness?)
}
