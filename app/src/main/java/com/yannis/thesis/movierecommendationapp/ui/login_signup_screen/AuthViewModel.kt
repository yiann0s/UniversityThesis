package com.yannis.thesis.movierecommendationapp.ui.login_signup_screen

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yannis.thesis.movierecommendationapp.MovieRecommendationApp
import com.yannis.thesis.movierecommendationapp.data.local.User
import com.yannis.thesis.movierecommendationapp.domain.repositories.UserRepository
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val loginSuccess: Boolean = false
)

class AuthViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun login(email: String, password: String) {
        val normalizedEmail = email.trim()
        when {
            normalizedEmail.isEmpty() -> showError("Email is required")
            !Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches() ->
                showError("Must provide valid email")
            password.isEmpty() -> showError("Password is required")
            else -> viewModelScope.launch {
                _uiState.value = AuthUiState(isLoading = true)
                val result = withContext(Dispatchers.IO) {
                    val user = userRepository.findByEmail(normalizedEmail)
                    val passwordUser = userRepository.findByPassword(password)
                    when {
                        user == null -> Result.failure(IllegalArgumentException("No such username exists"))
                        passwordUser == null -> Result.failure(IllegalArgumentException("Wrong password :S"))
                        else -> Result.success(passwordUser.id)
                    }
                }
                result.onSuccess { userId ->
                    MovieRecommendationApp.getInstance().loggedInUserId = userId
                    _uiState.value = AuthUiState(loginSuccess = true)
                }.onFailure { error ->
                    _uiState.value = AuthUiState(errorMessage = error.message)
                }
            }
        }
    }

    fun signup(email: String, password: String) {
        val normalizedEmail = email.trim()
        when {
            normalizedEmail.isEmpty() -> showError("Must provide an email address")
            !Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches() ->
                showError("Must provide a valid email")
            password.isEmpty() -> showError("Mus provide a password")
            else -> viewModelScope.launch {
                _uiState.value = AuthUiState(isLoading = true)
                val result = withContext(Dispatchers.IO) {
                    if (userRepository.findByEmail(normalizedEmail) != null) {
                        Result.failure(IllegalArgumentException("This email is already in use"))
                    } else {
                        userRepository.insert(
                            User(UUID.randomUUID().toString(), null, normalizedEmail, password)
                        )
                        Result.success(Unit)
                    }
                }
                result.onSuccess {
                    _uiState.value = AuthUiState(successMessage = "Registration complete")
                }.onFailure { error ->
                    _uiState.value = AuthUiState(errorMessage = error.message)
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    fun clearLoginSuccess() {
        _uiState.value = _uiState.value.copy(loginSuccess = false)
    }

    private fun showError(message: String) {
        _uiState.value = AuthUiState(errorMessage = message)
    }
}

class AuthViewModelFactory(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
