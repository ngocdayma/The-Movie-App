package com.example.movieinfo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieinfo.repository.AuthRepository
import com.example.movieinfo.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _registerResult = MutableStateFlow<Result<String>?>(null)
    val registerResult: StateFlow<Result<String>?> = _registerResult

    fun register(email: String, password: String) {
        viewModelScope.launch {
            val result = authRepository.register(email, password)
            _registerResult.value = result

            result.onSuccess { uid ->
                userRepository.createUserIfNotExists(uid)
            }
        }
    }
}
