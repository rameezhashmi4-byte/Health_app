package com.pushprime.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth: FirebaseAuth? = try {
        FirebaseAuth.getInstance()
    } catch (_: Exception) {
        null
    }

    val currentUser: FirebaseUser?
        get() = auth?.currentUser

    fun addAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        auth?.addAuthStateListener(listener)
    }

    fun removeAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        auth?.removeAuthStateListener(listener)
    }

    suspend fun signInWithEmail(email: String, password: String) {
        val firebaseAuth = auth ?: throw IllegalStateException("Firebase unavailable")
        firebaseAuth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun createAccount(email: String, password: String) {
        val firebaseAuth = auth ?: throw IllegalStateException("Firebase unavailable")
        firebaseAuth.createUserWithEmailAndPassword(email, password).await()
    }

    suspend fun signInWithGoogle(idToken: String) {
        val firebaseAuth = auth ?: throw IllegalStateException("Firebase unavailable")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential).await()
    }

    fun signOut() {
        auth?.signOut()
    }
}
