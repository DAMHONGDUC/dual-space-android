package com.duplicateapp.gamespace.features.workspace.domain

import kotlinx.coroutines.flow.StateFlow

interface WorkspaceRepository {
    val sessions: StateFlow<List<GameSession>>
    val selectedSessionId: StateFlow<String?>

    suspend fun selectSession(sessionId: String)
    suspend fun recordSessionOpened(sessionId: String, openedAtEpochMillis: Long)
    suspend fun updateSessionIdentity(sessionId: String, name: String, accountColor: AccountColor)
    suspend fun addSession(
        name: String,
        gameName: String,
        packageName: String,
        profileTarget: ProfileTarget,
        virtualUserId: Int,
    )
    suspend fun deleteSession(sessionId: String)
}
