package com.duplicateapp.gamespace.features.workspace.domain

data class game_session(
    val id: String,
    val name: String,
    val game_name: String,
    val state: session_state,
    val cpu_percent: Int,
    val memory_gb: Float,
    val temperature_celsius: Int,
    val frames_per_second: Int,
    val package_name: String,
    val profile_target: profile_target,
)

enum class profile_target {
    personal,
    managed,
}

enum class session_state {
    starting,
    running,
    paused,
    stopped,
    failed,
}
