package com.duplicateapp.gamespace.features.auth.domain

data class AuthSession(
    val userId: String,
    val displayName: String?,
    val email: String?,
)

interface AuthRepository {
    fun currentSession(): AuthSession?
    suspend fun authenticateGoogleIdToken(idToken: String): AuthSession?
    fun signOut()
}
