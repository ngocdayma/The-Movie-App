package com.example.movieinfo.viewmodel

import android.content.Context
import androidx.lifecycle.liveData
import androidx.lifecycle.*
import com.example.movieinfo.repository.AuthRepository
import com.example.movieinfo.repository.UserRepository
import com.example.movieinfo.util.WatchlistManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _email = MutableLiveData<String>()
    val email: LiveData<String> get() = _email

    private val _savedMoviesCount = MutableLiveData<Int>()
    val savedMoviesCount: LiveData<Int> get() = _savedMoviesCount

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _isGoogleUser = MutableLiveData<Boolean>()
    val isGoogleUser: LiveData<Boolean> get() = _isGoogleUser

    fun loadUserData(context: Context) {
        val uid = authRepository.getCurrentUserId()
        if (uid == null) {
            _error.value = "User not logged in"
            return
        }

        viewModelScope.launch {
            try {
                // Lấy email
                val userEmail = authRepository.getCurrentUserEmail()
                    ?: userRepository.getUserEmail(uid)
                    ?: "No Email"
                _email.value = userEmail

                // Kiểm tra đăng nhập bằng Google
                val googleUser = authRepository.isGoogleUser()
                _isGoogleUser.value = googleUser

                // Lấy số lượng phim trong watchlist
                val savedCount = WatchlistManager.getWatchlistCount(context)
                _savedMoviesCount.value = savedCount
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) = liveData(Dispatchers.IO) {
        emit(Resource.Loading)
        try {
            if (_isGoogleUser.value == true) {
                emit(Resource.Error("Cannot change password for Google account!"))
                return@liveData
            }
            val result = authRepository.changePassword(currentPassword, newPassword)
            if (result.isSuccess) emit(Resource.Success(true))
            else emit(Resource.Error(result.exceptionOrNull()?.message ?: "Unknown error"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    fun logout(): LiveData<Resource<Boolean>> = liveData(Dispatchers.IO) {
        emit(Resource.Loading)
        try {
            val result = authRepository.logout()
            if (result.isSuccess) {
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error(result.exceptionOrNull()?.message ?: "Logout failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }
}
