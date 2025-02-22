package com.picturestore.service

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.picturestore.data.User
import kotlinx.coroutines.tasks.await

class AuthService {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    val currentUser get() = auth.currentUser

    suspend fun signIn(email: String, password: String): FirebaseUser? {
        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            return result.user
        } catch (e: FirebaseAuthException) {
            throw Exception(handleAuthError(e))
        }
    }

    suspend fun signUp(email: String, password: String): FirebaseUser? {
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()

            val user= User.empty(result.user!!.uid, result.user!!.email ?: "")
            UserService().createInitialUser(user).await()

            result.user!!.sendEmailVerification().await()
            return result.user
        } catch (e: FirebaseAuthException) {
            throw Exception(handleAuthError(e))
        }
    }

    suspend fun checkEmailVerification() {
        auth.currentUser?.reload()?.await()
    }

    suspend fun resendVerificationEmail() {
        auth.currentUser?.sendEmailVerification()?.await()
    }

    suspend fun deleteUser(password: String) {
        val user = auth.currentUser
        if (user != null) {
            try {
                val email = user.email ?: throw Exception("User email not found")

                val credential = EmailAuthProvider.getCredential(email, password)
                user.reauthenticate(credential).await()

                firestore.collection("users").document(user.uid).delete().await()

                user.delete().await()

                signOut()
            } catch (e: FirebaseAuthException) {
                throw Exception(handleAuthError(e))
            } catch (e: Exception) {
                throw Exception("User deletion error: ${e.message}")
            }
        }
    }

    suspend fun signOut() {
        auth.signOut()
    }

    private fun handleAuthError(e: FirebaseAuthException): String {
        return when (e.errorCode) {
            "ERROR_REQUIRES_RECENT_LOGIN" -> "Request recent login"
            "ERROR_UNVERIFIED_EMAIL" -> "Email unverified"
            "ERROR_INVALID_EMAIL" -> "Invalid email"
            "ERROR_USER_DISABLED" -> "User disabled"
            "ERROR_USER_NOT_FOUND" -> "User not found"
            "ERROR_WRONG_PASSWORD" -> "Wrong password"
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Email already used"
            "ERROR_WEAK_PASSWORD" -> "Password is weak"
            "ERROR_INVALID_CREDENTIAL" -> "Check login or password"
            else -> "Auth error: ${e.message}"
        }
    }
}