package com.inventory.system.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventory.system.domain.model.Result
import com.inventory.system.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val isLoggedIn: StateFlow<Boolean?> = authRepository.isLoggedIn()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "يرجى تعبئة جميع الحقول") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.login(username, password)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, success = true) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun register(username: String, email: String, password: String, confirmPassword: String) {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "يرجى تعبئة جميع الحقول") }
            return
        }
        if (password != confirmPassword) {
            _uiState.update { it.copy(error = "كلمتا المرور غير متطابقتين") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.register(username, email, password)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, success = true) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
