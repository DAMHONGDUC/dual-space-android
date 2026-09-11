package com.duplicateapp.gamespace.features.workspace.domain

data class GameSession(
    val id: String,
    val name: String,
    val gameName: String,
    val accountColor: AccountColor,
    val lastOpenedAtEpochMillis: Long?,
    val packageName: String,
    val profileTarget: ProfileTarget,
)

enum class AccountColor {
    blue,
    green,
    orange,
    purple,
}

enum class ProfileTarget {
    personal,
    managed,
}
