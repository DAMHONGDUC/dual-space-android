package com.duplicateapp.gamespace.features.workspace.domain

interface GameCopyRemover {
    fun remove(session: GameSession): Boolean
}
