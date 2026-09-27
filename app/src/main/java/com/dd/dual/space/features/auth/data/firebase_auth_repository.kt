package com.dd.dual.space.features.auth.data

import android.content.Context
import com.dd.dual.space.core.logging.AppLogger
import com.dd.dual.space.features.auth.domain.AuthRepository
import com.dd.dual.space.features.auth.domain.AuthSession
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class FirebaseAuthRepository(context: Context) : AuthRepository {
    private val firebaseAuth: FirebaseAuth? = if (FirebaseApp.getApps(context).isEmpty()) null else FirebaseAuth.getInstance()

    override fun currentSession(): AuthSession? = firebaseAuth?.currentUser?.toAuthSession()

    override suspend fun authenticateGoogleIdToken(idToken: String): AuthSession? {
        require(idToken.isNotBlank())
        AppLogger.action("authenticate_google", emptyMap())
        val auth: FirebaseAuth = firebaseAuth ?: return unavailable()
        return suspendCancellableCoroutine { continuation ->
            auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val session: AuthSession? = auth.currentUser?.toAuthSession()
                    AppLogger.success("authenticate_google", mapOf("authenticated" to (session != null)))
                    if (continuation.isActive) continuation.resume(session)
                } else {
                    val error: Exception = task.exception ?: IllegalStateException("Firebase authentication failed")
                    AppLogger.error("authenticate_google", error, emptyMap())
                    if (continuation.isActive) continuation.resume(null)
                }
            }
        }
    }

    override fun signOut() {
        AppLogger.action("sign_out", emptyMap())
        firebaseAuth?.signOut()
        AppLogger.success("sign_out", emptyMap())
    }

    private fun unavailable(): AuthSession? {
        AppLogger.error("authenticate_google", IllegalStateException("Firebase is not configured"), emptyMap())
        return null
    }

    private fun FirebaseUser.toAuthSession(): AuthSession = AuthSession(uid, displayName, email)
}
