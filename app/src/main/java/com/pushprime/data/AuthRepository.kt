package com.pushprime.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private fun firebaseAuthOrNull(): FirebaseAuth? {
        return try {
            FirebaseAuth.getInstance()
        } catch (_: Exception) {
            null
        }
    }

    private fun firebaseAuth(): FirebaseAuth {
        return firebaseAuthOrNull()
            ?: throw IllegalStateException("Firebase is not initialized. Check google-services.json.")
    }

    val currentUser: FirebaseUser?
        get() = firebaseAuthOrNull()?.currentUser

    fun addAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        firebaseAuthOrNull()?.addAuthStateListener(listener)
    }

    fun removeAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        firebaseAuthOrNull()?.removeAuthStateListener(listener)
    }

    suspend fun signInWithEmail(email: String, password: String) {
        firebaseAuth().signInWithEmailAndPassword(email, password).await()
    }

    suspend fun createAccount(email: String, password: String) {
        firebaseAuth().createUserWithEmailAndPassword(email, password).await()
    }

    suspend fun signInWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth().signInWithCredential(credential).await()
    }

    fun signOut() {
        firebaseAuthOrNull()?.signOut()
    }
}
