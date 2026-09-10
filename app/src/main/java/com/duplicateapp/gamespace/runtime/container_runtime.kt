package com.duplicateapp.gamespace.runtime

import android.view.Surface

interface container_runtime {
    suspend fun start(space_id: String, surface: Surface): runtime_session
    suspend fun pause(session_id: String)
    suspend fun stop(session_id: String)
}

data class runtime_session(
    val id: String,
    val process_id: Int,
)
