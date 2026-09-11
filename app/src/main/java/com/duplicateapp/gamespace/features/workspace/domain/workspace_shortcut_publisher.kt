package com.duplicateapp.gamespace.features.workspace.domain

interface WorkspaceShortcutPublisher {
    fun publish(sessions: List<GameSession>)
}
