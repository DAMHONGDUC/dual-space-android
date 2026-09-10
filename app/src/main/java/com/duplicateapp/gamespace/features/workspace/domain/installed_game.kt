package com.duplicateapp.gamespace.features.workspace.domain

data class InstalledGame(
    val label: String,
    val packageName: String,
    val profileTarget: ProfileTarget,
)

interface GameCatalog {
    fun listInstalledGames(profileTarget: ProfileTarget): List<InstalledGame>
}
