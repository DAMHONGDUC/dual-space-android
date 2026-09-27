package com.dd.dual.space.features.workspace.domain

interface WorkspaceShortcutPublisher {
    fun publish(sessions: List<GameSession>)
}
