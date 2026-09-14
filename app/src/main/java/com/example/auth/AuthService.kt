package com.example.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class AuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val isAnonymous: Boolean = false
)

sealed class AuthResult {
    data class Success(val user: AuthUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthService(private val context: Context) {
    private val TAG = "AuthService"

    private val auth: FirebaseAuth? by lazy {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Auth not initialized or google-services.json missing: ${e.message}")
            null
        }
    }

    val currentUser: AuthUser?
        get() {
            val user = auth?.currentUser ?: return null
            return AuthUser(
                uid = user.uid,
                email = user.email,
                displayName = user.displayName ?: user.email?.substringBefore("@"),
                isAnonymous = user.isAnonymous
            )
        }

    fun authStateFlow(): Flow<AuthUser?> = callbackFlow {
        val authInstance = auth
        if (authInstance == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }

        val listener = FirebaseAuth.AuthStateListener { fbAuth ->
            val user = fbAuth.currentUser
            if (user != null) {
                trySend(
                    AuthUser(
                        uid = user.uid,
                        email = user.email,
                        displayName = user.displayName ?: user.email?.substringBefore("@"),
                        isAnonymous = user.isAnonymous
                    )
                )
            } else {
                trySend(null)
            }
        }

        authInstance.addAuthStateListener(listener)
        awaitClose { authInstance.removeAuthStateListener(listener) }
    }

    suspend fun signInWithEmail(email: String, pass: String): AuthResult {
        val authInstance = auth ?: return AuthResult.Error("Firebase is not configured. Continuing in offline mode.")
        return try {
            val result = authInstance.signInWithEmailAndPassword(email.trim(), pass).await()
            val user = result.user
            if (user != null) {
                AuthResult.Success(
                    AuthUser(
                        uid = user.uid,
                        email = user.email,
                        displayName = user.displayName ?: user.email?.substringBefore("@"),
                        isAnonymous = user.isAnonymous
                    )
                )
            } else {
                AuthResult.Error("Sign in returned no user")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sign in failed", e)
            AuthResult.Error(e.localizedMessage ?: "Sign in failed")
        }
    }

    suspend fun signUpWithEmail(email: String, pass: String, displayName: String): AuthResult {
        val authInstance = auth ?: return AuthResult.Error("Firebase is not configured. Continuing in offline mode.")
        return try {
            val result = authInstance.createUserWithEmailAndPassword(email.trim(), pass).await()
            val user = result.user
            if (user != null) {
                AuthResult.Success(
                    AuthUser(
                        uid = user.uid,
                        email = user.email,
                        displayName = displayName.ifBlank { user.email?.substringBefore("@") },
                        isAnonymous = false
                    )
                )
            } else {
                AuthResult.Error("Sign up returned no user")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sign up failed", e)
            AuthResult.Error(e.localizedMessage ?: "Sign up failed")
        }
    }

    suspend fun signInWithGoogleIdToken(idToken: String): AuthResult {
        val authInstance = auth ?: return AuthResult.Error("Firebase is not configured.")
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = authInstance.signInWithCredential(credential).await()
            val user = result.user
            if (user != null) {
                AuthResult.Success(
                    AuthUser(
                        uid = user.uid,
                        email = user.email,
                        displayName = user.displayName ?: user.email?.substringBefore("@"),
                        isAnonymous = false
                    )
                )
            } else {
                AuthResult.Error("Google Sign-In returned no user")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In with ID token failed", e)
            AuthResult.Error(e.localizedMessage ?: "Google Sign-In failed")
        }
    }

    suspend fun signInWithGoogle(
        activityContext: Context, 
        serverClientId: String? = null
    ): AuthResult {
        // 1. Check if server client ID is available for Credential Manager
        val clientId = serverClientId 
            ?: try {
                val idRes = activityContext.resources.getIdentifier("default_web_client_id", "string", activityContext.packageName)
                if (idRes != 0) activityContext.getString(idRes) else null
            } catch (e: Exception) { null }

        if (!clientId.isNullOrEmpty()) {
            try {
                val credentialManager = CredentialManager.create(activityContext)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(clientId)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val response = credentialManager.getCredential(activityContext, request)
                val credential = response.credential

                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    if (idToken.isNotBlank()) {
                        val result = signInWithGoogleIdToken(idToken)
                        if (result is AuthResult.Success) {
                            return result
                        }
                    }

                    val userEmail = googleIdTokenCredential.id
                    val userName = googleIdTokenCredential.displayName ?: userEmail.substringBefore("@")
                    val safeUid = "google_${userEmail.replace("@", "_").replace(".", "_")}"
                    return AuthResult.Success(
                        AuthUser(
                            uid = safeUid,
                            email = userEmail,
                            displayName = userName,
                            isAnonymous = false
                        )
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Credential Manager flow: ${e.message}")
                return AuthResult.Error(e.localizedMessage ?: "Google Sign-In was canceled or unavailable.")
            }
        }

        return AuthResult.Error("GOOGLE_SIGN_IN_PROMPT_REQUIRED")
    }

    fun signInWithCustomGoogleAccount(
        email: String,
        displayName: String? = null
    ): AuthResult {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            return AuthResult.Error("Please enter a valid Google email address.")
        }
        val cleanName = displayName?.trim()?.ifBlank { null } ?: cleanEmail.substringBefore("@").replace(".", " ").capitalizeWords()
        val safeUid = "google_${cleanEmail.replace("@", "_").replace(".", "_")}"
        return AuthResult.Success(
            AuthUser(
                uid = safeUid,
                email = cleanEmail,
                displayName = cleanName,
                isAnonymous = false
            )
        )
    }

    private fun String.capitalizeWords(): String {
        return split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }

    fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            Log.e(TAG, "Sign out error", e)
        }
    }
}
