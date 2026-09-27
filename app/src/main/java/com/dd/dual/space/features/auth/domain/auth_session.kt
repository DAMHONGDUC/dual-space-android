package com.dd.dual.space.features.auth.domain

data class AuthSession(
    val userId: String,
    val displayName: String?,
    val email: String?,
)

enum class AccountDeletionResult {
    deleted,
    requiresRecentSignIn,
    failed,
}

interface AuthRepository {
    fun currentSession(): AuthSession?
    suspend fun authenticateGoogleIdToken(idToken: String): AuthSession?
    fun signOut()
    suspend fun deleteAccount(): AccountDeletionResult
}
