package com.inventory.system.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventory.system.domain.model.Result
import com.inventory.system.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isLoading: Boolean = false,
    val username: String = "",
    val email: String = "",
    val role: String = "",
    val userRole: String = "",
    val serverUrl: String = "http://10.0.2.2:8000/",
    val isChangingPassword: Boolean = false,
    val passwordError: String? = null,
    val logoutSuccess: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = authRepository.getMe()) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            username = result.data.username,
                            email = result.data.email,
                            role = getRoleDisplayName(result.data.role),
                            userRole = result.data.role
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        if (newPassword != confirmPassword) {
            _uiState.update { it.copy(passwordError = "كلمتا المرور غير متطابقتين") }
            return
        }
        if (newPassword.length < 6) {
            _uiState.update { it.copy(passwordError = "كلمة المرور يجب أن تكون 6 أحرف على الأقل") }
            return
        }
        
        _uiState.update { it.copy(passwordError = null, isChangingPassword = true) }
        
        // TODO: Implement password change API call when available
        // For now, just show success
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            _uiState.update { 
                it.copy(
                    isChangingPassword = false,
                    passwordError = null
                ) 
            }
        }
    }

    fun updateServerUrl(url: String) {
        _uiState.update { it.copy(serverUrl = url) }
        // Note: In a real app, this would be saved to DataStore
        // and the NetworkModule would read from there
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { it.copy(logoutSuccess = true) }
        }
    }

    private fun getRoleDisplayName(role: String): String {
        return when (role.lowercase()) {
            "admin" -> "مدير النظام"
            "manager" -> "مدير"
            "user" -> "مستخدم"
            else -> role
        }
    }
}
