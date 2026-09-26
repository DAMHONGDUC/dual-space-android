package com.dd.dual.space.features.workspace.domain

interface DiagnosticReporter {
    fun share(profileTarget: ProfileTarget, readiness: GameLaunchReadiness?)
}
