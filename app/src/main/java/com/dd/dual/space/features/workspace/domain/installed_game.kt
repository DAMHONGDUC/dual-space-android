package com.dd.dual.space.features.workspace.domain

data class InstalledGame(
    val label: String,
    val packageName: String,
    val profileTarget: ProfileTarget,
    val isCopyAvailable: Boolean = true,
)

interface GameCatalog {
    fun listInstalledGames(profileTarget: ProfileTarget): List<InstalledGame>
}

/** Keeps apps whose name or package id contains [query], ignoring case; a blank query keeps everything. */
fun List<InstalledGame>.filterByQuery(query: String): List<InstalledGame> {
    val normalizedQuery: String = query.trim()
    if (normalizedQuery.isEmpty()) return this
    return filter { game ->
        game.label.contains(normalizedQuery, ignoreCase = true) ||
            game.packageName.contains(normalizedQuery, ignoreCase = true)
    }
}
