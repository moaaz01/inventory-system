package com.inventory.system.presentation.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventory.system.domain.model.Result
import com.inventory.system.domain.model.User
import com.inventory.system.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UsersUiState(
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val error: String? = null,
    val actionSuccess: String? = null,
    val actionError: String? = null
)

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UsersUiState())
    val uiState: StateFlow<UsersUiState> = _uiState.asStateFlow()

    init { loadUsers() }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val r = userRepository.getUsers()) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, users = r.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = r.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun createUser(username: String, email: String, password: String, role: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, actionError = null) }
            when (val r = userRepository.createUser(username, email, password, role)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, actionSuccess = "تم إنشاء المستخدم بنجاح") }
                    loadUsers()
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, actionError = r.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun updateUserRole(id: Int, role: String) {
        viewModelScope.launch {
            when (val r = userRepository.updateUser(id, role = role)) {
                is Result.Success -> {
                    _uiState.update { it.copy(actionSuccess = "تم تحديث الدور") }
                    loadUsers()
                }
                is Result.Error -> _uiState.update { it.copy(actionError = r.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun toggleUserActive(id: Int, isActive: Boolean) {
        viewModelScope.launch {
            when (val r = userRepository.updateUser(id, isActive = isActive)) {
                is Result.Success -> {
                    _uiState.update { it.copy(actionSuccess = if (isActive) "تم تفعيل الحساب" else "تم تعطيل الحساب") }
                    loadUsers()
                }
                is Result.Error -> _uiState.update { it.copy(actionError = r.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun deleteUser(id: Int) {
        viewModelScope.launch {
            when (val r = userRepository.deleteUser(id)) {
                is Result.Success -> {
                    _uiState.update { it.copy(actionSuccess = "تم حذف المستخدم") }
                    loadUsers()
                }
                is Result.Error -> _uiState.update { it.copy(actionError = r.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun clearMessages() { _uiState.update { it.copy(actionSuccess = null, actionError = null) } }
}
