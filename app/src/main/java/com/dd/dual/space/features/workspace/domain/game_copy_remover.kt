package com.dd.dual.space.features.workspace.domain

interface GameCopyRemover {
    fun remove(session: GameSession): Boolean
}
