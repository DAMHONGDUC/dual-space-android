package com.duplicateapp.gamespace.features.auth.data

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import com.duplicateapp.gamespace.core.logging.AppLogger
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

class GoogleCredentialProvider(private val webClientId: String) {
    suspend fun getIdToken(activity: Activity): String? {
        AppLogger.action("request_google_credential", emptyMap())
        if (webClientId.isBlank()) {
            AppLogger.error("request_google_credential", IllegalStateException("Firebase web client ID is not configured"), emptyMap())
            return null
        }
        return try {
            val option = GetGoogleIdOption.Builder()
                .setServerClientId(webClientId)
                .setFilterByAuthorizedAccounts(false)
                .build()
            val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
            val credential = CredentialManager.create(activity).getCredential(activity, request).credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                GoogleIdTokenCredential.createFrom(credential.data).idToken.also {
                    AppLogger.success("request_google_credential", emptyMap())
                }
            } else {
                AppLogger.error("request_google_credential", IllegalStateException("Unsupported credential type"), emptyMap())
                null
            }
        } catch (error: NoCredentialException) {
            AppLogger.error("request_google_credential", error, mapOf("reason" to "no_credential"))
            null
        } catch (error: Exception) {
            AppLogger.error("request_google_credential", error, emptyMap())
            null
        }
    }
}
