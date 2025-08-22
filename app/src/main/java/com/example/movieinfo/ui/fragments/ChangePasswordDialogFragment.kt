package com.example.movieinfo.ui.dialogs

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.movieinfo.R
import com.example.movieinfo.databinding.DialogChangePasswordBinding
import com.example.movieinfo.repository.AuthRepository
import kotlinx.coroutines.launch

class ChangePasswordDialogFragment : DialogFragment() {

    private var _binding: DialogChangePasswordBinding? = null
    private val binding get() = _binding!!

    private val authRepository = AuthRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Toggle password visibility
        setupPasswordToggle(binding.edtCurrentPassword)
        setupPasswordToggle(binding.edtNewPassword)
        setupPasswordToggle(binding.edtConfirmPassword)

        binding.btnCancel.setOnClickListener { dismiss() }

        binding.btnSubmit.setOnClickListener {
            val current = binding.edtCurrentPassword.text.toString()
            val new = binding.edtNewPassword.text.toString()
            val confirm = binding.edtConfirmPassword.text.toString()

            if (current.isBlank() || new.isBlank() || confirm.isBlank()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (new != confirm) {
                Toast.makeText(requireContext(), "New passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Show progress
            binding.progressBar.visibility = View.VISIBLE
            binding.btnSubmit.isEnabled = false

            // Change password flow
            lifecycleScope.launch {
                try {
                    val email = authRepository.getCurrentUserEmail()
                    if (email == null) {
                        Toast.makeText(requireContext(), "You are not logged in", Toast.LENGTH_SHORT).show()
                        binding.progressBar.visibility = View.GONE
                        binding.btnSubmit.isEnabled = true
                        return@launch
                    }

                    // Re-authenticate current password
                    val result = authRepository.login(email, current)
                    if (result.isSuccess) {
                        authRepository.changePassword(new) { success, error ->
                            binding.progressBar.visibility = View.GONE
                            binding.btnSubmit.isEnabled = true
                            if (success) {
                                Toast.makeText(requireContext(), "Password changed successfully", Toast.LENGTH_SHORT).show()
                                dismiss()
                            } else {
                                Toast.makeText(requireContext(), error ?: "Error", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSubmit.isEnabled = true
                        Toast.makeText(requireContext(), "Current password is incorrect", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSubmit.isEnabled = true
                    Toast.makeText(requireContext(), e.message ?: "Error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupPasswordToggle(editText: EditText) {
        editText.setOnTouchListener { _, event ->
            val drawableEnd = 2
            if (event.rawX >= (editText.right - (editText.compoundDrawables[drawableEnd]?.bounds?.width()
                    ?: 0))
            ) {
                val isVisible = editText.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                if (isVisible) {
                    editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_hide_password, 0)
                } else {
                    editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_show_password, 0)
                }
                editText.setSelection(editText.text.length)
                true
            } else {
                false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
