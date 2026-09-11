package com.duplicateapp.gamespace.features.workspace.domain

data class InstalledGame(
    val label: String,
    val packageName: String,
    val profileTarget: ProfileTarget,
    val isCopyAvailable: Boolean = true,
)

interface GameCatalog {
    fun listInstalledGames(profileTarget: ProfileTarget): List<InstalledGame>
}
