package com.dd.dual.space.runtime

import android.view.Surface

interface ContainerRuntime {
    suspend fun start(spaceId: String, surface: Surface): RuntimeSession
    suspend fun pause(sessionId: String)
    suspend fun stop(sessionId: String)
}

data class RuntimeSession(
    val id: String,
    val processId: Int,
)
