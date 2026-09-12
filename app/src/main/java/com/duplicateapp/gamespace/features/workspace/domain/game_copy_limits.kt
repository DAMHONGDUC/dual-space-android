package com.duplicateapp.gamespace.features.workspace.domain

object GameCopyLimits {
    const val maximumCopiesPerGame: Int = 2
    val supportedVirtualUserIds: IntRange = 1..maximumCopiesPerGame

    fun nextAvailableVirtualUserId(usedVirtualUserIds: Set<Int>): Int? =
        supportedVirtualUserIds.firstOrNull { virtualUserId -> virtualUserId !in usedVirtualUserIds }
}
