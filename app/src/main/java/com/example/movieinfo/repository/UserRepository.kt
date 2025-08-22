package com.example.movieinfo.repository

import com.example.movieinfo.models.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val usersRef = db.collection("users")

    // Tạo user mới nếu chưa tồn tại
    suspend fun createUserIfNotExists(uid: String, email: String? = null) {
        val doc = usersRef.document(uid).get().await()
        if (!doc.exists()) {
            val user = User(
                uid = uid,
                email = email ?: "",
                movies = emptyList()
            )
            usersRef.document(uid).set(user).await()
        }
    }

    // Lấy thông tin user theo UID
    suspend fun getUser(uid: String): User? {
        val doc = usersRef.document(uid).get().await()
        return if (doc.exists()) doc.toObject(User::class.java) else null
    }

    // Lấy email của user
    suspend fun getUserEmail(uid: String): String? {
        val user = getUser(uid)
        return user?.email
    }

    // Lấy số lượng phim đã lưu của user
    suspend fun getSavedMoviesCount(uid: String): Int {
        val user = getUser(uid)
        return user?.movies?.size ?: 0
    }

    // Cập nhật danh sách phim yêu thích
    suspend fun updateFavoriteMovies(uid: String, movies: List<String>) {
        usersRef.document(uid).update("favoriteMovies", movies).await()
    }
}
