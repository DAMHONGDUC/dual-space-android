package com.duplicateapp.gamespace.features.workspace.domain

data class GameSession(
    val id: String,
    val name: String,
    val gameName: String,
    val state: SessionState,
    val cpuPercent: Int,
    val memoryGb: Float,
    val temperatureCelsius: Int,
    val framesPerSecond: Int,
    val packageName: String,
    val profileTarget: ProfileTarget,
)

enum class ProfileTarget {
    personal,
    managed,
}

enum class SessionState {
    starting,
    running,
    paused,
    stopped,
    failed,
}
