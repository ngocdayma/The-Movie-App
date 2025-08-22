package com.example.movieinfo.repository

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Đăng nhập bằng email/password
    suspend fun login(email: String, password: String): Result<String> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(auth.currentUser?.uid ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Đăng ký bằng email/password
    suspend fun register(email: String, password: String): Result<String> {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(auth.currentUser?.uid ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Quên mật khẩu
    suspend fun sendPasswordResetEmail(email: String): Result<Boolean> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Google Sign-In
    suspend fun signInWithGoogle(idToken: String): Result<String> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential).await()
            Result.success(auth.currentUser?.uid ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Boolean> {
        if (isGoogleUser()) return Result.failure(Exception("Cannot change password for Google account"))
        val user = auth.currentUser ?: return Result.failure(Exception("User not logged in"))
        val email = user.email ?: return Result.failure(Exception("Email not found"))

        return try {
            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            user.reauthenticate(credential).await()
            user.updatePassword(newPassword).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun changePassword(newPassword: String, callback: (Boolean, String?) -> Unit) {
        if (isGoogleUser()) {
            callback(false, "Cannot change password for Google account")
            return
        }
        val user = auth.currentUser
        if (user == null) {
            callback(false, "User not logged in")
            return
        }
        user.updatePassword(newPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) callback(true, null)
                else callback(false, task.exception?.message)
            }
    }

    fun isGoogleUser(): Boolean {
        val user: FirebaseUser = auth.currentUser ?: return false
        return user.providerData
            .filter { it.providerId != "firebase" }
            .any { it.providerId == GoogleAuthProvider.PROVIDER_ID }
    }

    fun getCurrentUserEmail(): String? = auth.currentUser?.email

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun logout(): Result<Boolean> {
        return try {
            auth.signOut()
            Result.success(true) // thành công
        } catch (e: Exception) {
            Result.failure(e)    // thất bại (rất hiếm khi xảy ra)
        }
    }

}
