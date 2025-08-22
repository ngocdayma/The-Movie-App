package com.example.movieinfo.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.movieinfo.databinding.ActivityProfileBinding
import com.example.movieinfo.repository.AuthRepository
import com.example.movieinfo.repository.UserRepository
import com.example.movieinfo.ui.dialogs.ChangePasswordDialogFragment
import com.example.movieinfo.viewmodel.ProfileViewModel
import com.example.movieinfo.viewmodel.ProfileViewModelFactory
import com.example.movieinfo.viewmodel.Resource

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(authRepository, userRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Back button
        binding.btnBack.setOnClickListener { finish() }

        // Logout
        binding.btnLogout.setOnClickListener {
            viewModel.logout().observe(this) { result ->
                when (result) {
                    is Resource.Success -> {
                        Toast.makeText(this, "Log out Success!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    is Resource.Error -> {
                        Toast.makeText(this, "Lỗi: ${result.message}", Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Loading -> {
                        // có thể show progress bar
                    }
                }
            }

        }

        // Observe ViewModel
        viewModel.email.observe(this) { email ->
            binding.tvEmail.text = email ?: "Email not found"
        }

        viewModel.savedMoviesCount.observe(this) { count ->
            binding.tvSavedMovies.text = "Movies in WatchList: $count"
        }

        viewModel.isGoogleUser.observe(this) { isGoogle ->
            // Nếu là Google user, vẫn hiện nút nhưng khi bấm sẽ hiện thông báo
            binding.btnChangePassword.setOnClickListener {
                if (isGoogle) {
                    Toast.makeText(this, "Cannot change password for Google account", Toast.LENGTH_SHORT).show()
                } else {
                    val dialog = ChangePasswordDialogFragment()
                    dialog.show(supportFragmentManager, "ChangePasswordDialog")
                }
            }
        }

        viewModel.error.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        // Load dữ liệu user
        viewModel.loadUserData(this)
    }
}
