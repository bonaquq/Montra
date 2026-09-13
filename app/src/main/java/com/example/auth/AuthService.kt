package com.example.auth

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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

    fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            Log.e(TAG, "Sign out error", e)
        }
    }
}
