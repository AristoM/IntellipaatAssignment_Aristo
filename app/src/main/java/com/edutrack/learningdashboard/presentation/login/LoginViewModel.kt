package com.edutrack.learningdashboard.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edutrack.learningdashboard.data.repository.CourseRepository
import com.edutrack.learningdashboard.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LoginUiState {
    object Idle : LoginUiState
    object Loading : LoginUiState
    data class Success(val user: User) : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel(
    private val repository: CourseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        val trimmedEmail = email.trim()
        val emailRegex = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$".toRegex()

        // Validation
        if (trimmedEmail.isEmpty()) {
            _uiState.value = LoginUiState.Error("Email address is required")
            return
        }

        if (!trimmedEmail.matches(emailRegex)) {
            _uiState.value = LoginUiState.Error("Please enter a valid email address (e.g. name@domain.com)")
            return
        }

        if (password.length < 6) {
            _uiState.value = LoginUiState.Error("Password must be at least 6 characters")
            return
        }

        _uiState.value = LoginUiState.Loading

        viewModelScope.launch {
            val result = repository.login(trimmedEmail, password)
            result.fold(
                onSuccess = { user ->
                    _uiState.value = LoginUiState.Success(user)
                },
                onFailure = { error ->
                    _uiState.value = LoginUiState.Error(error.localizedMessage ?: "Authentication failed")
                }
            )
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}
